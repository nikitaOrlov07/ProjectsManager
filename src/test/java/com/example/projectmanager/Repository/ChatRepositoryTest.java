package com.example.projectmanager.Repository;

import com.example.Model.Security.UserEntity;
import com.example.Repository.AttachmentRepository;
import com.example.Repository.ProjectRepository;
import com.example.Repository.Security.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // configure H2 database
public class ChatRepositoryTest {
    @Autowired
    private AttachmentRepository attachmentRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;
    UserEntity user = UserEntity.builder()
            .username("username")
            .email("email")
            .password("password")
            .build();

}
