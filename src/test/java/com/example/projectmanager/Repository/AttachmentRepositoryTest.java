package com.example.projectmanager.Repository;

import com.example.Model.Attachment;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Repository.AttachmentRepository;
import com.example.Repository.ProjectRepository;
import com.example.Repository.Security.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // configure H2 database
public class AttachmentRepositoryTest {
    @Autowired
    private AttachmentRepository attachmentRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private UserRepository userRepository;
    UserEntity  user = UserEntity.builder()
            .username("username")
            .email("email")
            .password("password")
            .build();
    Project project = Project.builder()
            .name("project")
            .description("description")
            .involvedUsers(Arrays.asList(user))
            .build();

    @Test
    public void  AttachmentRepository_saveProject_returnProject()
    {
        Attachment attachment = Attachment.builder()
                .creator(user.getUsername())
                .project(project)
                .data(new byte[2])
                .fileName("projectAttachment")
                .build();
        Attachment savedAttachment = attachmentRepository.save(attachment);
        Assertions.assertNotNull(savedAttachment);
        Assertions.assertEquals(attachment,savedAttachment);
    }
    @Test
    public void  AttachmentRepository_findByProject_returnProject()
    {
        Attachment attachment = Attachment.builder()
                .creator(user.getUsername())
                .project(project)
                .data(new byte[2])
                .fileName("projectAttachment")
                .build();
        attachmentRepository.save(attachment);
        Attachment returnedAttachment = attachmentRepository.findById(attachment.getId()).get();
        Assertions.assertNotNull(returnedAttachment);
        Assertions.assertEquals(attachment,returnedAttachment);
    }
    @Test
    public void AttachmentRepository_findAllByProject_returnProject()
    {
        projectRepository.save(project);
        Attachment attachment = Attachment.builder()
                .creator(user.getUsername())
                .project(project)
                .data(new byte[2])
                .fileName("projectAttachment")
                .build();
        attachmentRepository.save(attachment);
        List<Attachment> attachments = attachmentRepository.findAllByProject(project);

        Assertions.assertNotNull(attachments);
        Assertions.assertEquals(1, attachments.size());
        Assertions.assertEquals(attachment,attachments.get(0));
    }
    @Test
    public void AttachmentRepository_findAllByCreator_returnProject()
    {
        userRepository.save(user);
        Attachment attachment = Attachment.builder()
                .creator(user.getUsername())
                .data(new byte[2])
                .fileName("projectAttachment")
                .build();
        attachmentRepository.save(attachment);
        List<Attachment> attachments = attachmentRepository.findAllByCreator(user.getUsername());

        Assertions.assertNotNull(attachments);
        Assertions.assertEquals(1, attachments.size());
        Assertions.assertEquals(attachment,attachments.get(0));
    }

}
