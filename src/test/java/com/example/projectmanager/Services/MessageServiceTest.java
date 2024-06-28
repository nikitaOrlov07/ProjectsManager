package com.example.projectmanager.Services;

import com.example.Config.MessageType;
import com.example.Model.Chat;
import com.example.Model.Message;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Repository.MessageRepository;
import com.example.Repository.ProjectRepository;
import com.example.Repository.Security.UserRepository;
import com.example.Security.SecurityUtil;
import com.example.Service.ChatService;
import com.example.Service.TaskService;
import com.example.Service.impl.MessageServiceimpl;
import com.example.Service.impl.ProjectServiceimpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.plugins.MockitoPlugins;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MessageServiceTest {


    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;
    @Mock
    private ChatService chatService;
    @InjectMocks
    private MessageServiceimpl messageService;
    private Message message; private Chat chat; private UserEntity user;
    @BeforeEach
    public void setUp() {
        chat = Chat.builder()
                .id(1L)
                .messages(new ArrayList<>())
                .participants(new ArrayList<>())
                .build();
        message = Message.builder()
                .text("Message text")
                .type(MessageType.CHAT)
                .build();
        chatService.save(chat); messageRepository.save(message);
    }
    @Test
    public void MessageService_findAllChatMessage_ReturnMessages()
    {
        message.setChat(chat);
        messageRepository.save(message);
        when(messageRepository.findAllByChatId(Mockito.any(Long.class))).thenReturn(Arrays.asList(message));
        List<Message> returnedMessages = messageService.findAllChatMessage(chat.getId());

        Assertions.assertNotNull(returnedMessages);
        Assertions.assertEquals(message,returnedMessages.get(0));

    }
    @Test
    public void MessageService_saveMessage_returnSavedMessage()
    {
      UserEntity user = UserEntity.builder()
              .id(1L)
              .username("username")
              .password("password")
              .email("email")
              .chats(Arrays.asList(chat))
              .build();

      when(messageRepository.save(message)).thenReturn(message);
      when(chatService.findById(Mockito.any(Long.class))).thenReturn(Optional.ofNullable(chat));
      Message savedMessage = messageService.saveMessage(message,chat.getId(),user);

      Assertions.assertNotNull(savedMessage);
      Assertions.assertEquals(message,savedMessage);
    }
    @Test
    public void MessageService_findById_returnMessage()
    {
        when(messageRepository.findById(message.getId())).thenReturn(Optional.ofNullable(message));
        Optional<Message> returnedMessage = messageService.findById(message.getId());

        Assertions.assertTrue(returnedMessage.isPresent());
        Assertions.assertEquals(message, returnedMessage.get());
    }
    @Test
    public void MessageService_DeleteMessage_DeleteMessage() {
        // Arrange

        // Act
        messageRepository.delete(message);
        Optional<Message> returnedMessage = messageRepository.findById(message.getId());
        // Assert
        verify(messageRepository).delete(message);
        Assertions.assertTrue(returnedMessage.isEmpty());
    }
}
