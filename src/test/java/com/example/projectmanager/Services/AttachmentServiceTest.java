package com.example.projectmanager.Services;

import com.example.Model.Attachment;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Repository.AttachmentRepository;
import com.example.Service.ProjectService;
import com.example.Service.Security.UserService;
import com.example.Service.impl.AttachmentServiceimpl;
import org.junit.Before;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AttachmentServiceTest {
    @Mock
    private ProjectService projectService;
    @Mock
    private UserService userService;
    @Mock
    private MultipartFile file;
    @Mock
    private AttachmentRepository attachmentRepository;

    @InjectMocks
    private AttachmentServiceimpl attachmentService;
    private UserEntity user;
    private Project project;

    private Attachment attachment;

    @BeforeEach
    public void setUp() throws IOException {
       user = UserEntity.builder()
                .id(1L)
                .username("username")
                .password("password")
                .email("email")
                .build();
        project = Project.builder()
                .id(1L)
                .name("project")
                .description("project description")
                .category("project category")
                .involvedUsers(Arrays.asList(user))
                .attachments(new ArrayList<>())
                .build();
        // Mock MultipartFile behavior
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getBytes()).thenReturn("test content".getBytes());
    }
    @Test
    public void AttachmentService_saveAttachment_returnSavedAttachment() throws Exception {
        // Given
        Attachment expectedAttachment = new Attachment("test.txt", "text/plain", "test content".getBytes(), null, null);
        when(attachmentRepository.save(any(Attachment.class))).thenReturn(expectedAttachment);
        // When
        Attachment savedAttachment = attachmentService.saveAttachment(file, project, user);

        // Then
        Assertions.assertNotNull(savedAttachment);
        Assertions.assertEquals("test.txt", savedAttachment.getFileName());
        Assertions.assertEquals("text/plain", savedAttachment.getFileType());
        Assertions.assertArrayEquals("test content".getBytes(), savedAttachment.getData());
    }
    @Test
    public void AttachmentService_deleteAttachment_returnSavedAttachment()
    {

    }

}
