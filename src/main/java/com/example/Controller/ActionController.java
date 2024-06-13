package com.example.Controller;

import com.example.Model.Chat;
import com.example.Model.Message;
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
public class ActionController {
    private ProjectService projectService; private UserService userService; private MessageService messageService; private ChatService chatService;
    private static final Logger logger = LoggerFactory.getLogger(ActionController.class);

    @Autowired
    public ActionController(ProjectService projectService, UserService userService,MessageService messageService,ChatService chatService) {
        this.projectService = projectService;
        this.userService = userService;
        this.messageService=messageService;
        this.chatService=chatService;
    }
    @PostMapping("/users/addFriend/{friendId}")
    public String addFriend(@PathVariable("friendId") Long friendId)
    {
        UserEntity currentUser = userService.findByUsername(SecurityUtil.getSessionUser());
        UserEntity friendUser = userService.findById(friendId);
        userService.friendsLogic(currentUser,friendUser,"add");
        return "redirect:/home?friendAdded";
    }
    @PostMapping("/users/removeFriend/{friendId}")
    public String removeFriend(@PathVariable("friendId") Long friendId)
    {
        UserEntity currentUser = userService.findByUsername(SecurityUtil.getSessionUser());
        UserEntity friendUser = userService.findById(friendId);
        userService.friendsLogic(currentUser,friendUser,"remove");
        return "redirect:/home?friendRemoved";
    }

    //----------------------------------------------- Chat ------------------------------------------------------------------
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

    @GetMapping("/chat/{chatId}")
    public String getChat(@PathVariable("chatId") Long chatId, Model model)
    {
        if(SecurityUtil.getSessionUser() == null || SecurityUtil.getSessionUser().isEmpty())
        {
            return  "redirect:/home";
        }
        List<Message> messages = messageService.findAllChatMessage(chatId);
        UserEntity currentUser = userService.findByUsername(SecurityUtil.getSessionUser());
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
        return "redirect:/chat/" + chatId;
    }
}
