package com.example.projectmanager.Service;

import com.example.Model.Attachment;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Repository.AttachmentRepository;
import com.example.Service.ProjectService;
import com.example.Service.Security.UserService;
import com.example.Service.impl.AttachmentServiceimpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
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
    }
    @Test
    public void AttachmentService_saveAttachment_returnSavedAttachment() throws Exception {
        // Mock MultipartFile behavior
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getBytes()).thenReturn("test content".getBytes());


        Attachment expectedAttachment = new Attachment("test.txt", "text/plain", "test content".getBytes(), null, null);
        when(attachmentRepository.save(any(Attachment.class))).thenReturn(expectedAttachment);

        Attachment savedAttachment = attachmentService.saveAttachment(file, project, user);


        Assertions.assertNotNull(savedAttachment);
        Assertions.assertEquals("test.txt", savedAttachment.getFileName());
        Assertions.assertEquals("text/plain", savedAttachment.getFileType());
        Assertions.assertArrayEquals("test content".getBytes(), savedAttachment.getData());
    }
    @Test
    public void AttachmentService_deleteAttachment_shouldDeleteAttachment() throws Exception {
        // Arrange
        Long projectId = 1L;
        Long fileId = 1L;

        Attachment attachment = Attachment.builder()
                .id(fileId)
                .fileName("test.txt")
                .creator(user.getUsername())
                .project(project)
                .viewUrl("viewUrl")
                .downloadUrl("downloadUrl")
                .fileType("type")
                .data(new byte[2])
                .timestamp("")
                .build();

        Project project = Project.builder()
                .id(projectId)
                .attachments(new ArrayList<>(Arrays.asList(attachment)))
                .build();

        when(projectService.findById(projectId)).thenReturn(project);
        when(attachmentRepository.findById(fileId)).thenReturn(Optional.of(attachment));

        // Act
        attachmentService.deleteAttachment(projectId, fileId);

        // Assert
        verify(projectService).findById(projectId);
        verify(attachmentRepository).findById(fileId);
        verify(attachmentRepository).delete(attachment);
       Assertions.assertTrue(project.getAttachments().isEmpty());
    }
   @Test
   public void AttachmentService_getAttachment_ReturnAttachment() throws Exception {
       Attachment attachment = Attachment.builder()
               .id(1L)
               .fileName("test.txt")
               .creator(user.getUsername())
               .project(project)
               .viewUrl("viewUrl")
               .downloadUrl("downloadUrl")
               .fileType("type")
               .data(new byte[2])
               .timestamp("")
               .build();
       when(attachmentRepository.findById(1L)).thenReturn(Optional.ofNullable(attachment));

       Attachment returnedAttachment = attachmentService.getAttachment(attachment.getId());

       Assertions.assertNotNull(returnedAttachment);
       Assertions.assertEquals(attachment,returnedAttachment);
   }
    @Test
    public void AttachmentService_findAllByProject_ReturnAttachment()
   {
       Attachment attachment = Attachment.builder()
               .id(1L)
               .fileName("test.txt")
               .creator(user.getUsername())
               .project(project)
               .viewUrl("viewUrl")
               .downloadUrl("downloadUrl")
               .fileType("type")
               .data(new byte[2])
               .timestamp("")
               .build();
       when(attachmentRepository.findAllByProject(project)).thenReturn(Arrays.asList(attachment));

       List<Attachment> returnedAttachmentList =   attachmentService.findAllByProject(project);

      Assertions.assertNotNull(returnedAttachmentList);
      Assertions.assertEquals(attachment,returnedAttachmentList.get(0));
   }
}
