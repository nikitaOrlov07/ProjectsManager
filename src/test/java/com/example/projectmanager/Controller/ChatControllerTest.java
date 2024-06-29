package com.example.projectmanager.Controller;

import com.example.Config.MessageType;
import com.example.Controller.ChatController;
import com.example.Dto.DeleteMessageRequest;
import com.example.Model.Chat;
import com.example.Model.Message;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Service.ChatService;
import com.example.Service.MessageService;
import com.example.Service.ProjectService;
import com.example.Service.Security.UserService;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;

import java.security.Principal;
import java.util.*;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ChatController.class) // used for testing controllers , automatically configures MockMVC
@AutoConfigureMockMvc(addFilters = false)       //Spring Security filters will not be applied to tests
@ExtendWith(MockitoExtension.class)
public class ChatControllerTest {
    @Autowired
    private MockMvc mockMvc; // Implement MockMVC in an application , mockMVC is used only for HTTP endpoints
    @MockBean
    private ProjectService projectService;
    @MockBean
    private UserService userService;
    @MockBean
    private MessageService messageService;
    @MockBean
    private ChatService chatService;
    @MockBean
    private SimpMessagingTemplate messagingTemplate;
    @Autowired
    private ChatController chatController; // for Websocket methods

    private UserEntity user;  private UserEntity user2; private Chat chat; private Message message; private Project project;

    @BeforeEach
    public void init()
    {
        project = Project.builder()
                .id(1L)
                .name("projectName")
                .description("project description")
                .category("project category")
                .attachments(new ArrayList<>())
                .tasks(new ArrayList<>())
                .involvedUsers(new ArrayList<>())
                .build();

        user = UserEntity.builder()
                .id(1L)
                .username("username")
                .password("password")
                .email("email")
                .chats(new ArrayList<>())
                .build();
        user2 = UserEntity.builder()
                .id(2L)
                .username("username2")
                .password("password2")
                .email("email2")
                .chats(new ArrayList<>())
                .build();
        chat = Chat.builder()
                .id(1L)
                .participants(new ArrayList<>(Arrays.asList(user, user2)))
                .messages(new ArrayList<>())
                .build();
        message = Message.builder()
                .id(1L)
                .text("text")
                .author(user.getUsername())
                .type(MessageType.CHAT)
                .user(user)
                .chat(chat)
                .build();

        chat.getMessages().add(message);
        user.getChats().add(chat);
        user2.getChats().add(chat);
        project.getInvolvedUsers().add(user);
    }

    @Test
    @WithMockUser(username = "username")
    public void ChatController_findOrCreateChat() throws Exception {
        when(userService.findByUsername(user.getUsername())).thenReturn(user);
        when(userService.findById(user2.getId())).thenReturn(user2);
        when(chatService.findOrCreateChat(user, user2)).thenReturn(chat);

        // Act & Assert
        mockMvc.perform(get("/chat/findOrCreate/{secondId}", user2.getId()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/chat/" + chat.getId()));

        // Verify service method calls
        Mockito.verify(userService).findByUsername(user.getUsername());
        Mockito.verify(userService).findById(user2.getId());
        Mockito.verify(chatService).findOrCreateChat(user, user2);
    }
    @Test
    @WithMockUser(username = "username")
    public void ChatController_findProjectChat_returnChat() throws Exception {
        project.setChat(chat);
        when(userService.findByUsername(user.getUsername())).thenReturn(user);
        when(projectService.findById(project.getId())).thenReturn(project);

         mockMvc.perform(get("/project/{projectId}/chat/{chatId}",project.getId(),chat.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("chat"))
                .andReturn();

         verify(projectService).findById(project.getId());
    }
    @Test
    @WithMockUser(username="username")
    public void ChatController_findChatChat_returnChat() throws Exception {
        when(chatService.findById(chat.getId())).thenReturn(Optional.ofNullable(chat));
        when(userService.findByUsername(user.getUsername())).thenReturn(user);
        when(messageService.findAllChatMessage(chat.getId())).thenReturn(Arrays.asList(message));

        mockMvc.perform(get("/chat/{chatId}",chat.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("chat"))
                .andReturn();

        verify(messageService).findAllChatMessage(chat.getId());
        verify(userService).findByUsername(user.getUsername());
        verify(chatService).findById(chat.getId());
    }

    // can`t test Websocket method with MockMvc
    @Test
    public void ChatController_sendMessage_returnMessage()
    {
        Principal principal = () -> "username";
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create(SimpMessageType.MESSAGE);
        headerAccessor.setUser(principal);

        when(userService.findByUsername("username")).thenReturn(user);
        when(messageService.saveMessage(message, chat.getId(),user )).thenReturn(message);

        // Act
        Message result = chatController.sendMessage(chat.getId(), message, headerAccessor); // test Websocket controller method

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(message.getAuthor(), result.getAuthor());
        Assertions.assertEquals(message.getText(), result.getText());
        Assertions.assertNotNull(result.getPubDate());
        Assertions.assertEquals(user, result.getUser());

        verify(userService).findByUsername("username");
        verify(messageService).saveMessage(message, chat.getId(), user);
    }
    @Test
    public void ChatController_DeleteChat_UserInChat() {
        // Arrange
        Principal principal = () -> "username";


        when(userService.findByUsername("username")).thenReturn(user);
        when(chatService.findById(chat.getId())).thenReturn(Optional.of(chat));

        // Act
        Message result = chatController.deleteChat(chat.getId(), principal);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(MessageType.CHAT_DELETED, result.getType());
        Assertions.assertEquals("username", result.getAuthor());
        Assertions.assertEquals("Chat deleted", result.getText());

        verify(chatService, Mockito.times(1)).delete(chat);
    }
    @Test
    public void ChatController_DeleteChat_UserNotInChat()
    {
        // Arrange
        chat.getParticipants().remove(user);
        Principal principal = () -> "username";
        when(userService.findByUsername(user.getUsername())).thenReturn(user);
        when(chatService.findById(chat.getId())).thenReturn(Optional.ofNullable(chat));
        // Act
        Message returnedMessage = chatController.deleteChat(chat.getId(),principal);
        // Assert
        Assert.assertNull(returnedMessage);
        verify(userService).findByUsername(user.getUsername());
        verify(chatService).findById(chat.getId());

    }
    @Test
    public void ChatController_deleteMessage_SucesfulDelete()
    {
        DeleteMessageRequest request = new DeleteMessageRequest(message.getId());
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>());
        headerAccessor.getSessionAttributes().put("username", "username");

        when(userService.findByUsername("username")).thenReturn(user);
        when(messageService.findById(message.getId())).thenReturn(Optional.of(message));
        when(chatService.findById(chat.getId())).thenReturn(Optional.of(chat));

        // Act
        Message result = chatController.deleteMessage(chat.getId(), request, headerAccessor);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals(MessageType.DELETE, result.getType());
        Assertions.assertEquals("username", result.getAuthor());
        Assertions.assertEquals(message.getId(), result.getId());
        Assertions.assertEquals(message.getId().toString(), result.getText());

        verify(messageService, Mockito.times(1)).deleteMessage(message, user, chat);
    }
    @Test
    public void testDeleteMessage_ChatNotFound() {
        // Arrange
        Long chatId = 2L;
        Long messageId = 2L;
        DeleteMessageRequest request = new DeleteMessageRequest();
        SimpMessageHeaderAccessor headerAccessor = SimpMessageHeaderAccessor.create();
        headerAccessor.setSessionAttributes(new HashMap<>());
        headerAccessor.getSessionAttributes().put("username", "username");

        when(userService.findByUsername("username")).thenReturn(user);
        when(messageService.findById(messageId)).thenReturn(Optional.of(message));
        when(chatService.findById(chatId)).thenReturn(Optional.empty()); // don`t have this chat and return empty

        // Act & Assert
        Assertions.assertThrows(NoSuchElementException.class, () -> chatController.deleteMessage(chatId, request, headerAccessor)); //  checks that a call chatController.deleteMessage(chatId, request, headerAccessor) method will throw a NoSuchElementException exception.
    }




}
