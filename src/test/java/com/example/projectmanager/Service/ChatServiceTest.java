package com.example.projectmanager.Service;

import com.example.Config.MessageType;
import com.example.Model.Chat;
import com.example.Model.Message;
import com.example.Model.Security.UserEntity;
import com.example.Repository.ChatRepository;
import com.example.Service.MessageService;
import com.example.Service.ProjectService;
import com.example.Service.Security.UserService;
import com.example.Service.impl.ChatServiceimpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ChatServiceTest {
   @Mock
   private UserService userService;
   @Mock
   private ProjectService projectService;
   @Mock
   private MessageService messageService;
   @Mock
   private ChatRepository chatRepository;

   @InjectMocks
   private ChatServiceimpl chatService;

    private UserEntity user;  private UserEntity user2; private Chat chat; private Message message;

    @BeforeEach
    public void setUp()
    {
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
                .text("text")
                .author(user.getUsername())
                .type(MessageType.CHAT)
                .user(user)
                .chat(chat)
                .build();

        chat.getMessages().add(message);
        user.getChats().add(chat);
        user2.getChats().add(chat);
    }
   @Test
   public void ChatService_findOrCreate_ReturnChat()
   {
       when(userService.findById(user.getId())).thenReturn(user);
       when(userService.findById(user2.getId())).thenReturn(user2);
       when(chatRepository.findByParticipantsContains(user)).thenReturn(Arrays.asList(chat));

       Chat returnedChat = chatService.findOrCreateChat(user,user2);

       Assertions.assertNotNull(returnedChat);
       Assertions.assertEquals(chat,returnedChat);
   }
   @Test
   public void ChatService_findAllByParticipants_ReturnChat() {
       when(chatRepository.findByParticipantsContains(user)).thenReturn(Arrays.asList(chat));

       List<Chat> chats = chatService.findAllByParticipants(user);

       Assertions.assertNotNull(chats);
       Assertions.assertEquals(chat,chats.get(0));
   }
  @Test
  public void ChatService_findByIdBy_ReturnChat()
  {
     when(chatRepository.findById(chat.getId())).thenReturn(Optional.of(chat));

     Optional<Chat> returnedChat = chatService.findById(1L);

     Assertions.assertTrue(returnedChat.isPresent());
     Assertions.assertEquals(chat,returnedChat.get());
  }


    @Test
    public void ChatService_DeleteChat_doNothing() {

        // Arrange
        Mockito.doNothing().when(messageService).deleteMessage(any(Message.class), any(UserEntity.class), any(Chat.class));

        // Act
        chatService.delete(chat);

        // Assert
        verify(messageService).deleteMessage(eq(message), eq(user), eq(chat));
        verify(chatRepository).delete(chat);
        Assertions.assertTrue(user.getChats().isEmpty());
      Assertions.assertTrue(user2.getChats().isEmpty());
      Assertions.assertTrue(chat.getParticipants().isEmpty());
      Assertions.assertTrue(chat.getMessages().isEmpty());
    }

    @Test
    public void ChatService_ClearChat_DoesRemoveAllMessages()
    {
        // Arrange
        doNothing().when(messageService).deleteMessage(any(Message.class), any(UserEntity.class), any(Chat.class));
        when(chatRepository.findById(any(Long.class))).thenReturn(Optional.of(chat));

        // Ensure chat has at least one message
        int initialMessageCount = chat.getMessages().size();
        Assertions.assertTrue(initialMessageCount > 0);

        // Act
        chatService.clearMessages(chat);

        // Assert
        verify(messageService, times(initialMessageCount)).deleteMessage(any(Message.class), any(UserEntity.class), eq(chat));
        verify(chatRepository).findById(chat.getId());
        Assertions.assertTrue(chat.getMessages().isEmpty());
    }

}


