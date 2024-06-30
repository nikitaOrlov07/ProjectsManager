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
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
                .startDate("2005-02-25")
                .endDate("2005-02-26")
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
                .startDate(LocalDate.parse("2005-02-25"))
                .endDate(LocalDate.parse("2005-02-26"))
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
    @WithMockUser(username = "username")
    void ActionController_updateProject_UserInvolved() throws Exception {
        project.getInvolvedUsers().add(user);
        when(userService.findByUsername("username")).thenReturn(user);
        when(projectService.findById(projectDto.getId())).thenReturn(project);

        mockMvc.perform(post("/projects/update/save")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", projectDto.getId().toString())
                        .param("name", projectDto.getName())
                        .param("description", projectDto.getDescription())
                        .param("category", projectDto.getCategory())
                        .param("startDate", projectDto.getStartDate() != null ? projectDto.getStartDate().toString() : "")
                        .param("endDate", projectDto.getEndDate() != null ? projectDto.getEndDate().toString() : "")
                        .param("password", projectDto.getPassword()))
                        .andExpect(status().isOk())
                        .andReturn();
    }
}

