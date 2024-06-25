package com.example.projectmanager.Repository;

import com.example.Model.Chat;
import com.example.Model.Security.UserEntity;
import com.example.Repository.AttachmentRepository;
import com.example.Repository.ChatRepository;
import com.example.Repository.ProjectRepository;
import com.example.Repository.Security.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // configure H2 database
public class ChatRepositoryTest {
    @Autowired
    private AttachmentRepository attachmentRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ChatRepository chatRepository;
    UserEntity user = UserEntity.builder()
            .username("username")
            .email("email")
            .password("password")
            .build();
    @Test
    public void ChatRepository_saveChat_returnSaved()
    {
        Chat chat = Chat.builder()
                .participants(Arrays.asList(user))
                .build();

         Chat savedChat = chatRepository.save(chat);

        Assertions.assertNotNull(savedChat);
        Assertions.assertEquals(chat,savedChat);
    }
    @Test
    public void ChatRepository_findById_returnChat()
    {
        Chat chat = Chat.builder()
                .participants(Arrays.asList(user))
                .build();

       chatRepository.save(chat);
       Chat returnedChat = chatRepository.findById(chat.getId()).get();

       Assertions.assertNotNull(returnedChat);
       Assertions.assertEquals(chat,returnedChat);
    }
    @Test
    public void ChatRepository_deleteChat_returnNull()
    {
        Chat chat = Chat.builder()
                .participants(Arrays.asList(user))
                .build();

        chatRepository.save(chat);
        chatRepository.delete(chat);
        Optional<Chat> returnedChat = chatRepository.findById(chat.getId());// return an empty Optional

        Assertions.assertTrue(returnedChat.isEmpty());
    }
    @Test
    public void ChatRepository_findByParticipantsContains_returnChat()
    {
        Chat chat = Chat.builder()
                .participants(Arrays.asList(user))
                .build();
        chatRepository.save(chat);
        List<Chat> chats = chatRepository.findByParticipantsContains(user);

        Assertions.assertNotNull(chats);
        Assertions.assertEquals(chat,chats.get(0));
    }

}
