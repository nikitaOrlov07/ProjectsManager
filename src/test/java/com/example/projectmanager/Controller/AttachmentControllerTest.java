package com.example.projectmanager.Controller;

import com.example.Controller.AttachmentController;
import com.example.Controller.TaskController;
import com.example.Model.Attachment;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Model.Task;
import com.example.Service.AttachmentService;
import com.example.Service.ProjectService;
import com.example.Service.Security.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.ContentResultMatchers;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import static org.hamcrest.Matchers.containsString;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@WebMvcTest(controllers = AttachmentController.class) // used for testing controllers , automatically configures MockMVC
@AutoConfigureMockMvc(addFilters = false)       //Spring Security filters will not be applied to tests
@ExtendWith(MockitoExtension.class)
public class AttachmentControllerTest {
    @Autowired
    private MockMvc mockMvc; // Implement MockMVC in an application
    @MockBean
    private AttachmentService attachmentService;
    @MockBean
    private ProjectService projectService;
    @MockBean
    private UserService userService;
    @MockBean
    private MultipartFile file;
    private UserEntity user; private Project project; private Attachment attachment;

    @BeforeEach
    private void init()
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
        project.getInvolvedUsers().add(user);
    }
    @Test
    @WithMockUser(username = "username")
    public void AttachmentController_uploadFile_returnStatusFound() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "test content".getBytes()
        );

        Mockito.when(projectService.findById(1L)).thenReturn(project);
        Mockito.when(userService.findByUsername("username")).thenReturn(user);
        Mockito.when(attachmentService.saveAttachment(file,project, user)).thenReturn(new Attachment());

        mockMvc.perform(multipart("/upload/{projectId}", "1")
                        .file(file)) //  is used to add a file to a multipart request. This is a method specifically designed to simulate file uploading in tests.
                .andExpect(status().isFound())
                .andExpect(header().string("Location", Matchers.containsString("/projects/1")));
        verify(attachmentService).saveAttachment(file,project, user);
        verify(attachmentService).updateAttachmentUrls(Mockito.any(), Mockito.any(String.class),Mockito.any(String.class));
    }
    @Test
    @WithMockUser(username = "username")
    public void AttachmentController_downloadFile_returnStatusOk() throws Exception {
        Long fileId = 1L;
        String fileName = "test.txt";
        String fileContent = "test content";
        String fileType = "text/plain";

        attachment = Attachment.builder()
                .id(fileId)
                .fileName(fileName)
                .fileType(fileType)
                .data(fileContent.getBytes())
                .project(project)
                .build();

        Mockito.when(attachmentService.getAttachment(fileId)).thenReturn(attachment);
        Mockito.when(userService.findByUsername("username")).thenReturn(user);

        mockMvc.perform(get("/download/{fileId}", fileId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(fileType))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment;filename=\"" + fileName + "\"")) // Check that the response to the request contains a certain header
                .andExpect(content().bytes(fileContent.getBytes()));       // make sure the contents are the same

        verify(attachmentService).getAttachment(fileId); // check that the function is called with a certain parameter
        verify(userService).findByUsername("username");
    }
    @Test
    @WithMockUser(username = "username")
    public void AttachmentController_viewFile_returnStatusOk() throws Exception
    {
        Long fileId = 1L;
        String fileName = "test.txt";
        String fileContent = "test content";
        String fileType = "text/plain";

        attachment = Attachment.builder()
                .id(fileId)
                .fileName(fileName)
                .fileType(fileType)
                .data(fileContent.getBytes())
                .project(project)
                .build();

        Mockito.when(attachmentService.getAttachment(fileId)).thenReturn(attachment);
        Mockito.when(userService.findByUsername("username")).thenReturn(user);

        mockMvc.perform(get("/view/{fileId}", fileId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(fileType))
                .andExpect(content().bytes(fileContent.getBytes()));       // make sure the contents are the same

        verify(attachmentService, Mockito.times(2)).getAttachment(fileId); // This method is called twice
        verify(userService).findByUsername("username");
    }

    @Test
    @WithMockUser(username = "username")
    public void AttachmentController_deleteFile_returnStatusFound() throws Exception
    {
        Long projectId = 1L;
        Long fileId = 1L;

        Mockito.when(userService.findByUsername("username")).thenReturn(user);
        Mockito.when(projectService.findById(projectId)).thenReturn(project);
        Mockito.doNothing().when(attachmentService).deleteAttachment(projectId, fileId);

        mockMvc.perform(post("/projects/{projectId}/delete/{fileId}", projectId, fileId))
                .andExpect(status().isFound())
                .andExpect(header().string("Location",
                        Matchers.containsString("/projects/" + projectId))) // check the url to which the redirection will take place
                .andExpect(header().string("Location",
                        Matchers.containsString("fileDeleteSuccessfully"))); // check param in redirectURL

        verify(userService).findByUsername("username");
        verify(projectService).findById(projectId);
        verify(attachmentService).deleteAttachment(projectId, fileId);
    }




}
