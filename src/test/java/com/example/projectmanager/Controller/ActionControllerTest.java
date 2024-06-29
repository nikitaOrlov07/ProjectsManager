package com.example.projectmanager.Controller;

import com.example.Controller.ActionController;
import com.example.Controller.MainController;
import com.example.Dto.ProjectDto;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Security.SecurityUtil;
import com.example.Service.ChatService;
import com.example.Service.MessageService;
import com.example.Service.ProjectService;
import com.example.Service.Security.UserService;
import com.example.mappers.ProjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ActionController.class) // used for testing controllers , automatically configures MockMVC
@AutoConfigureMockMvc(addFilters = false)       //Spring Security filters will not be applied to tests
@ExtendWith(MockitoExtension.class)
public class ActionControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private ProjectService projectService;
    @MockBean
    private UserService userService;
    @MockBean
    private ProjectMapper projectMapper;
    private ObjectMapper objectMapper = new ObjectMapper();

    @MockBean
    private MessageService messageService;
    @MockBean
    private ChatService chatService;
    private Project project; private UserEntity user; private ProjectDto projectDto;
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
                .currentProjects(new ArrayList<>())
                .build();
        projectDto= ProjectDto.builder()
                .id(1L)
                .name("projectDtoName")
                .description("projectDto description")
                .category("projectDto category")
                .involvedUsers(new ArrayList<>())
                .build();
    }

    @Test
    @WithMockUser(username = "username")
    public void ActionController_AddProject_AddProject() throws Exception {
        when(userService.findByUsername(user.getUsername())).thenReturn(user);
        when(projectService.findById(project.getId())).thenReturn(project);


        mockMvc.perform(post("/projects/addProject/{projectId}", project.getId()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/projects/" + project.getId()));

        Assertions.assertTrue(user.getCurrentProjects().contains(project));
        verify(userService).findByUsername(user.getUsername());
        verify(projectService, Mockito.times(2)).findById(project.getId());
    }
    @Test
    @WithMockUser(username = "username")
    public void ActionController_RemoveProject() throws Exception {
        project.getInvolvedUsers().add(user);
        when(userService.findByUsername(user.getUsername())).thenReturn(user);
        when(projectService.findById(project.getId())).thenReturn(project);
        mockMvc.perform(post("/projects/removeProject/{projectId}", project.getId()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/projects/" + project.getId()));

        Assertions.assertTrue(user.getCurrentProjects().isEmpty());
    }
    @Test
    @WithMockUser(username = "user1")
    void ActionController_updateProject_UserInvolved() throws Exception {

        projectDto.getInvolvedUsers().add(user);
        when(userService.findByUsername("user1")).thenReturn(user);

        mockMvc.perform(post("/project/update/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDto)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects/1"));
        verify(projectService).save(Mockito.any(Project.class));
    }

    @Test
    @WithMockUser(username = "username2")
    void ActionController_updateProject_UserNotInvolved() throws Exception {

        when(userService.findByUsername("username2")).thenReturn(new UserEntity());


        mockMvc.perform(post("/project/update/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDto)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/home?operationError"));

        verify(projectService, Mockito.never()).save(Mockito.any(Project.class));
    }
    @Test
    @WithMockUser(username = "username")
    public void ActionController_deleteProject_RedirectToHome() throws Exception {
        project.getInvolvedUsers().add(user);
        when(userService.findByUsername(Mockito.any(String.class))).thenReturn(user);
        when(projectService.findById(project.getId())).thenReturn(project);
        doNothing().when(projectService).delete(Mockito.any(Project.class));

        mockMvc.perform(post("/projects/{projectId}/delete",project.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(projectDto)))
                .andExpect(status().is3xxRedirection()) // redirect
                .andExpect(redirectedUrl("/home?projectSuccessfulDelete"));

        verify(projectService).delete(Mockito.any(Project.class));
    }

}

