package com.example.projectmanager.Controller;

import com.example.Controller.TaskController;
import com.example.Dto.TaskDto;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Model.Task;
import com.example.Service.ProjectService;
import com.example.Service.Security.UserService;
import com.example.Service.TaskService;
import com.example.mappers.TaskMapper;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.ContentResultMatchers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TaskController.class) // used for testing controllers , automatically configures MockMVC
@AutoConfigureMockMvc(addFilters = false)       //Spring Security filters will not be applied to tests
@ExtendWith(MockitoExtension.class)
public class TaskControllerTest {
    @Autowired
    private MockMvc mockMvc; // Implement MockMVC in an application
    @MockBean
    private UserService userService;
    @MockBean
    private ProjectService projectService;
    @MockBean
    private TaskService taskService;
    private UserEntity user; private Project project; private Task task; private TaskDto taskDto;
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

        task = Task.builder()
                .id(1L)
                .complete(false)
                .name("taskName")
                .description("taskDescription")
                .project(project)
                .build();
        taskDto = TaskDto.builder()
                .id(1L)
                .complete(false)
                .name("taskDtoName")
                .description("taskDtoDescription")
                .project(project)
                .build();
        project.getInvolvedUsers().add(user);
        project.getTasks().add(task);
    }
    @Test
    @WithMockUser(username = "username")
    public void TaskController_CompleteTask_RedirectToProjectPage() throws Exception {
        when(userService.findByUsername(user.getUsername())).thenReturn(user);
        when(taskService.findById(task.getId())).thenReturn(task);
        ResultActions resultActions = mockMvc.perform(post("/taskComplete/1")
                        .param("complete", "true"))
                .andExpect(status().isFound()); // redirecting the client to another URL

        // Verify the redirect URL
        String expectedRedirectURL = "/projects/" + project.getId();
        resultActions.andExpect(MockMvcResultMatchers.redirectedUrl(expectedRedirectURL));

        // Verify that the response status is not 500 Internal Server Error
        MvcResult result = resultActions.andReturn();
        Assertions.assertNotEquals(500, result.getResponse().getStatus());

    }
    @Test
    public void TaskController_ResetTasks_RedirectToProjectPage() throws Exception {
        when(projectService.findById(project.getId())).thenReturn(project);
        ResultActions resultActions = mockMvc.perform(post("/projects/1/tasks/reset")
                        .param("complete", "true"))
                .andExpect(status().isFound()); // redirecting the client to another URL

        // Verify the redirect URL
        String expectedRedirectURL = "/projects/" + project.getId();
        resultActions.andExpect(MockMvcResultMatchers.redirectedUrl(expectedRedirectURL));

        // Verify that the response status is not 500 Internal Server Error
        MvcResult result = resultActions.andReturn();
        Assertions.assertNotEquals(500, result.getResponse().getStatus());


    }

    @Test
    @WithMockUser(username = "username")
    public void TaskController_createTask_Success() throws Exception {
        when(userService.findByUsername("username")).thenReturn(user);
        when(projectService.findById(1L)).thenReturn(project);
        when(taskService.save(Mockito.any(Task.class))).thenReturn(new Task());

        mockMvc.perform(post("/tasks/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(taskDto))
                        .param("projectId", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("success"));

        Mockito.verify(taskService, Mockito.times(1)).save(Mockito.any(Task.class));
    }












}
