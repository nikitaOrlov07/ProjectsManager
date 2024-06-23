package com.example.Controller;

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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
        return "chat";
    }
    @PostMapping("/messages/{chatId}/save")
    public String addComment(@ModelAttribute("message") Message message, @PathVariable("chatId") Long chatId, RedirectAttributes redirectAttributes) {
        String username = SecurityUtil.getSessionUser();
        if (username == null) // if the user is not authorized
        {
            redirectAttributes.addFlashAttribute("loginError", "You must be logged in");
            return "redirect:/login";
        }
        UserEntity user = userService.findByUsername(username);
        user.getMessages().add(message);
        // For current date time
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String formattedDateTime = currentDateTime.format(formatter);
        message.setPubDate(formattedDateTime);

        message.setUser(user);


        messageService.saveMessage(message, chatId);
        logger.info("Message was successfully saved");
        return "redirect:/chat/" + chatId;
    }
    @PostMapping("/comments/{chatId}/delete/{messageId}")
    public String deleteMessage(@PathVariable("messageId") Long messageId, @PathVariable("chatId") Long chatId) {
        // Take comment from database and delete it with service method and from users comment list
        Message message = messageService.findById(messageId).get();
        UserEntity user = userService.findById(message.getUser().getId());
        Chat chat = chatService.findById(chatId).get();
        user.getMessages().remove(message);
        messageService.deleteMessage(message,user,chat);
        logger.info("Message was successfully deleted");
        return "redirect:/chat/" + chatId;
    }
    @PostMapping("/chat/{chatId}/delete")
    public String deleteChat(@PathVariable("chatId") Long chatId)
    {
        Chat chat = chatService.findById(chatId).get();
        if(chat == null)
        {
            return "redirect/:home?operationError";
        }
        chatService.delete(chat);
        return "redirect:/home?chatDeleteSuccessfully";
    }
    @PostMapping("/chat/{chatId}/clear")
    public String clearChat(@PathVariable("chatId") Long chatId)
    {
        UserEntity currentUser = userService.findByUsername(SecurityUtil.getSessionUser());
        Chat chat = chatService.findById(chatId).get();
        if(chat == null || !chat.getParticipants().contains(currentUser) || chat.getMessages().isEmpty())
        {
            return "redirect/:home?operationError";
        }
        chatService.clearMessages(chat);
        logger.info("Chat was cleared  successfully");
        return "redirect:/chat/"+chatId;
    }
}
