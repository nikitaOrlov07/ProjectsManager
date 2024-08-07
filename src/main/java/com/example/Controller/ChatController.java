package com.example.Controller;


import com.example.Config.MessageType;
import com.example.Dto.DeleteMessageRequest;
import com.example.Model.Chat;
import com.example.Model.Message;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Security.SecurityUtil;
import com.example.Service.ChatService;
import com.example.Service.MessageService;
import com.example.Service.ProjectService;
import com.example.Service.Security.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class ChatController {
    private ProjectService projectService;
    private UserService userService;
    private MessageService messageService;
    private ChatService chatService;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    public ChatController(ProjectService projectService, UserService userService, MessageService messageService, ChatService chatService) {
        this.projectService = projectService;
        this.userService = userService;
        this.messageService = messageService;
        this.chatService = chatService;
    }

    // find existing chat or create new beetween two people for "home-page"
    @GetMapping("/chat/findOrCreate/{secondId}")
    public String findOrCreateChat(@PathVariable("secondId") Long secondId, Model model) {
        String currentUsername = SecurityUtil.getSessionUser();
        if (currentUsername == null || currentUsername.isEmpty()) {
            return "redirect:/home";
        }

        UserEntity currentUser = userService.findByUsername(currentUsername);
        UserEntity secondUser = userService.findById(secondId);

        Chat chat = chatService.findOrCreateChat(currentUser, secondUser);
        return "redirect:/chat/" + chat.getId();
    }

    @GetMapping("/project/{projectId}/chat/{chatId}")
    public String getChat(@PathVariable("chatId") Long chatId,
                          Model model,
                          @PathVariable("projectId") Long projectId) throws JsonProcessingException {
        UserEntity currentUser = userService.findByUsername(SecurityUtil.getSessionUser());
        Project project = projectService.findById(projectId);
        List<Message> messages = project.getChat().getMessages();
        if (SecurityUtil.getSessionUser() == null || SecurityUtil.getSessionUser().isEmpty() || (!project.getInvolvedUsers().contains(currentUser))) {
            return "redirect:/home";
        }
        model.addAttribute("messages", messages);
        model.addAttribute("messagesJson", new ObjectMapper().writeValueAsString(messages));
        model.addAttribute("chat",project.getChat());
        model.addAttribute("user", currentUser);
        return "chat";
    }

    @GetMapping("/chat/{chatId}")
    public String getChat(@PathVariable("chatId") Long chatId, Model model) throws JsonProcessingException {
        Chat chat = chatService.findById(chatId).get();
        List<Message> messages = messageService.findAllChatMessage(chatId);
        UserEntity currentUser = userService.findByUsername(SecurityUtil.getSessionUser());
        if (SecurityUtil.getSessionUser() == null || SecurityUtil.getSessionUser().isEmpty() || (!chat.getParticipants().contains(currentUser))) {
            return "redirect:/home";
        }
        model.addAttribute("messages", messages);
        model.addAttribute("messagesJson", new ObjectMapper().writeValueAsString(messages));
        model.addAttribute("user", currentUser);
        model.addAttribute("participants", chat.getParticipants().remove(currentUser));
        model.addAttribute("chat",chat);
        return "chat";
    }

    @MessageMapping("/chat/{chatId}/sendMessage")
    @SendTo("/topic/chat/{chatId}")
    public Message sendMessage(@DestinationVariable Long chatId, @Payload Message message,
                               SimpMessageHeaderAccessor headerAccessor) {
        String username = SecurityUtil.getSessionUser(headerAccessor.getUser());
        if (username == null) {
            throw new IllegalStateException("User not authenticated");
        }

        UserEntity user = userService.findByUsername(username);

        message.setUser(user);
        message.setPubDate(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        messageService.saveMessage(message, chatId, user);

        return message;
    }

    @MessageMapping("/chat/{chatId}/addUser")
    @SendTo("/topic/chat/{chatId}")
    public Message addUser(@DestinationVariable Long chatId, @Payload Message message,
                           SimpMessageHeaderAccessor headerAccessor) {
        String username = message.getAuthor();
        headerAccessor.getSessionAttributes().put("username", username);

        UserEntity currentUser = userService.findByUsername(username);
        Chat chat = chatService.findById(chatId).orElseThrow();

        Project project = projectService.findProjectByChat(chat);

        // Check if the user is already in the chat
        if (!chat.getParticipants().contains(currentUser) && project == null) {
            UserEntity otherUser = userService.findById(
                    chat.getParticipants().stream()
                            .filter(u -> !u.getUsername().equals(username))
                            .findFirst()
                            .orElseThrow()
                            .getId()
            );

            chat = chatService.findOrCreateChat(currentUser, otherUser);

            // if a new chat was created -> update id
            if (!chat.getId().equals(chatId)) {
                message.setChat(chat);
            }

            return message;
        } else {
            // user is already a member of the chat
            return null;
        }
    }

    @MessageMapping("/chat/{chatId}/delete")
    @SendTo("/topic/chat/{chatId}")
    public Message deleteChat(@DestinationVariable Long chatId, Principal principal) {
        String username = SecurityUtil.getSessionUser(principal);
        UserEntity user = userService.findByUsername(username);
        Chat chat = chatService.findById(chatId).orElseThrow();
        logger.info("Delete chat controller method is working");

        // implement "List.contains()" logic -> if i use regular contains method -> won`t working
        boolean userInChat = chat.getParticipants().stream()
                .anyMatch(participant -> participant.getId().equals(user.getId()));

        boolean chatInUserChats = user.getChats().stream()
                .anyMatch(userChat -> userChat.getId().equals(chat.getId()));

        if (chat != null && userInChat && chatInUserChats) {
            chatService.delete(chat);
            logger.info("Chat was deleted successfully");
            return Message.builder()
                    .type(MessageType.CHAT_DELETED)
                    .author(username)
                    .text("Chat deleted")
                    .build();
        } else {
            logger.warn("Error deleting chat. User in chat: {}, Chat in user chats: {}", userInChat, chatInUserChats);
            return null;
        }
    }
    @MessageMapping("/chat/{chatId}/deleteMessage")
    @SendTo("/topic/chat/{chatId}")
    @Transactional
    public Message deleteMessage(@DestinationVariable Long chatId, @Payload DeleteMessageRequest request,
                                 SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        UserEntity user = userService.findByUsername(username);
        Message message = messageService.findById(request.getMessageId()).orElseThrow();
        Chat chat = chatService.findById(chatId).orElseThrow();
        logger.info("Delete message controller in working");
        if (message != null && chat != null && message.getUser().equals(user)) {
            messageService.deleteMessage(message, user, chat);
            logger.info("Message deleted successfully");
            return Message.builder()
                    .type(MessageType.DELETE)
                    .author(username)
                    .id(request.getMessageId())
                    .text(request.getMessageId().toString())
                    .build();
        } else {
            logger.warn("Could not delete message. User mismatch or message/chat not found.");
            return null;
        }
    }


    @MessageMapping("/chat/{chatId}/clear")
    @SendTo("/topic/chat/{chatId}")
    public Message clearChat(@DestinationVariable Long chatId,
                             SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        UserEntity currentUser = userService.findByUsername(username);
        Chat chat = chatService.findById(chatId).orElseThrow();
        Project project  = projectService.findProjectByChat(chat);

        // implement "List.contains()" logic -> if i use regular contains method -> won`t working
        boolean userInChat = chat.getParticipants().stream()
                .anyMatch(participant -> participant.getId().equals(currentUser.getId()));

        boolean chatInUserChats = currentUser.getChats().stream()
                .anyMatch(userChat -> userChat.getId().equals(chat.getId()));

        boolean userInProject = false;
        if(project != null) {
          userInProject = project.getInvolvedUsers().stream()
                    .anyMatch(user -> user.getId().equals(currentUser.getId()));

        }

        logger.info("Chat clearing controller method is working");
        if (chat != null && ((chatInUserChats && userInChat) || (project != null && userInProject))) {
            chatService.clearMessages(chat);
            return Message.builder()
                    .type(MessageType.CLEAR)
                    .author(username)
                    .text("Chat was cleared")
                    .build();
        }


        logger.warn("Chat clearing failed");
        return null;
    }
}