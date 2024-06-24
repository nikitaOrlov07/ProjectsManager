package com.example.Controller;

import com.example.Config.ChatMessage;
import com.example.Config.MessageType;
import com.example.Model.Chat;
import com.example.Model.Message;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Security.SecurityUtil;
import com.example.Service.ChatService;
import com.example.Service.MessageService;
import com.example.Service.ProjectService;
import com.example.Service.Security.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
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
    private ProjectService projectService; private UserService userService; private MessageService messageService; private ChatService chatService;
    private static final Logger logger = LoggerFactory.getLogger(ChatController.class);

    @Autowired
    public ChatController(ProjectService projectService, UserService userService,MessageService messageService,ChatService chatService) {
        this.projectService = projectService;
        this.userService = userService;
        this.messageService=messageService;
        this.chatService=chatService;
    }
    // find existing chat or create new beetween two people
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
                          @PathVariable("projectId") Long projectId)
    {
        List<Message> messages = messageService.findAllChatMessage(chatId);
        UserEntity currentUser = userService.findByUsername(SecurityUtil.getSessionUser());
        Project project = projectService.findById(projectId);
        if(SecurityUtil.getSessionUser() == null || SecurityUtil.getSessionUser().isEmpty() || (!project.getInvolvedUsers().contains(currentUser)))
        {
            return  "redirect:/home";
        }
        model.addAttribute("messages", messages);
        model.addAttribute("user",currentUser);
        return "chat";
    }
    @GetMapping("/chat/{chatId}")
    public String getChat(@PathVariable("chatId") Long chatId, Model model)
    {
        Chat chat = chatService.findById(chatId).get();
        List<Message> messages = messageService.findAllChatMessage(chatId);
        UserEntity currentUser = userService.findByUsername(SecurityUtil.getSessionUser());
        if(SecurityUtil.getSessionUser() == null || SecurityUtil.getSessionUser().isEmpty() || (!chat.getParticipants().contains(currentUser)))
        {
            return  "redirect:/home";
        }
        model.addAttribute("messages", messages);
        model.addAttribute("user",currentUser);
        model.addAttribute("participants",chat.getParticipants().remove(currentUser));
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

        messageService.saveMessage(message, chatId ,user);

        return message;
    }

    @MessageMapping("/chat/{chatId}/addUser")
    @SendTo("/topic/chat/{chatId}")
    public ChatMessage addUser(@DestinationVariable Long chatId, @Payload ChatMessage chatMessage,
                               SimpMessageHeaderAccessor headerAccessor) {
        String username = chatMessage.getSender();
        headerAccessor.getSessionAttributes().put("username", username);

        UserEntity currentUser = userService.findByUsername(username);
        Chat chat = chatService.findById(chatId).orElseThrow();

        // Check , is the user already in the chat
        if (!chat.getParticipants().contains(currentUser)) {
            UserEntity otherUser = userService.findById(
                    chat.getParticipants().stream()
                            .filter(u -> !u.getUsername().equals(username))
                            .findFirst()
                            .orElseThrow()
                            .getId()
            );

            chat = chatService.findOrCreateChat(currentUser, otherUser);

            // if was created a new chat -> update id
            if (!chat.getId().equals(chatId)) {
                chatMessage.setChatId(chat.getId());
            }

            return chatMessage;
        } else {
            // user is already a member of the chat
            return null;
        }
    }
    @MessageMapping("/chat/{chatId}/deleteMessage")
    @SendTo("/topic/chat/{chatId}")
    public ChatMessage deleteMessage(@DestinationVariable Long chatId, @Payload Long messageId,
                                     SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        UserEntity user = userService.findByUsername(username);
        Message message = messageService.findById(messageId).orElseThrow();
        Chat chat = chatService.findById(chatId).orElseThrow();

        if (message.getUser().equals(user)) {
            messageService.deleteMessage(message, user, chat);
            return ChatMessage.builder()
                    .type(MessageType.DELETE)
                    .sender(username)
                    .content(messageId.toString())
                    .build();
        }

        return null;
    }

    @MessageMapping("/chat/{chatId}/clear")
    @SendTo("/topic/chat/{chatId}")
    public ChatMessage clearChat(@DestinationVariable Long chatId,
                                 SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        UserEntity user = userService.findByUsername(username);
        Chat chat = chatService.findById(chatId).orElseThrow();

        if (chat.getParticipants().contains(user)) {
            chatService.clearMessages(chat);
            return ChatMessage.builder()
                    .type(MessageType.CLEAR)
                    .sender(username)
                    .build();
        }

        return null;
    }

    @MessageMapping("/chat/{chatId}/delete")
    @SendTo("/topic/chat/{chatId}")
    public ChatMessage deleteChat(@DestinationVariable Long chatId,
                                  SimpMessageHeaderAccessor headerAccessor) {
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        UserEntity user = userService.findByUsername(username);
        Chat chat = chatService.findById(chatId).orElseThrow();

        if (chat.getParticipants().contains(user)) {
            chatService.delete(chat);
            return ChatMessage.builder()
                    .type(MessageType.DELETE_CHAT)
                    .sender(username)
                    .build();
        }

        return null;
    }
}