package com.example.projectmanager.Controller;


import com.example.Controller.MainController;
import com.example.Model.Chat;
import com.example.Model.Project;
import com.example.Model.Security.RoleEntity;
import com.example.Model.Security.UserEntity;
import com.example.Service.AttachmentService;
import com.example.Service.ProjectService;
import com.example.Service.Security.UserService;
import com.example.Service.TaskService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(controllers = MainController.class) // used for testing controllers , automatically configures MockMVC
@AutoConfigureMockMvc(addFilters = false)       //Spring Security filters will not be applied to tests
@ExtendWith(MockitoExtension.class)
public class MainControllerTest {
    @Autowired
    private MockMvc mockMvc; // Implement MockMVC in an application
    @MockBean
    private ProjectService projectService;
    @MockBean
    private UserService userService;
    @MockBean
    private TaskService taskService;
    @MockBean
    private AttachmentService attachmentService;
    private Project project; private UserEntity user; private RoleEntity adminRole;private Chat chat;
    private  Authentication authentication = mock(Authentication.class);
    @BeforeEach
    private void init()
    {
        chat = Chat.builder()
                .id(1L)
                .messages(new ArrayList<>())
                .project(project)
                .build();
        project = Project.builder()
                .id(1L)
                .name("projectName")
                .description("project description")
                .category("project category")
                .attachments(new ArrayList<>())
                .tasks(new ArrayList<>())
                .involvedUsers(new ArrayList<>())
                .chat(chat)
                .build();
        user = UserEntity.builder()
                .id(1L)
                .username("username")
                .password("password")
                .email("email")
                .chats(new ArrayList<>())
                .roles(new ArrayList<>())
                .build();

        adminRole= RoleEntity.builder()
                .id(1L)
                .name("ADMIN")
                .users(new ArrayList<>())
                .build();

        user.getRoles().add(adminRole);
        project.getInvolvedUsers().add(user);
    }
    @Test
    @WithMockUser(username = "username") // 1 way: @WithMockUser creates a fictional authenticated user in the Spring security context for the test. Without it, the SecurityContext (to retrieve the name of the current authenticated user) will return null
    public void MainController_mainPage_GetAllInformation() throws Exception {
        // Arrange
        when(projectService.findAllProjects()).thenReturn(Arrays.asList(project));
        when(projectService.findUsersProjects()).thenReturn(Arrays.asList(project));
        when(userService.findByUsername(Mockito.any(String.class))).thenReturn(user);

        // Act
        MvcResult result = mockMvc.perform(get("/home"))
                .andExpect(status().isOk())
                .andExpect(view().name("home-page"))
                .andReturn();

        // Assert
        ModelAndView modelAndViewContainer = result.getModelAndView();
        List<Project> projects = (List<Project>) modelAndViewContainer.getModel().get("allProjects");
        Assertions.assertNotNull(projects);
        Assertions.assertEquals(project, projects.get(0));
    }
    @Test
    public void MainController_detailPage_GetDetailPage() throws Exception {
        // 2 way to get authenticated user
        Authentication authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("username");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        when(userService.findByUsername("username")).thenReturn(user);
        when(projectService.findById(project.getId())).thenReturn(project);

        MvcResult result = mockMvc.perform(get("/projects/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("detail-page"))
                .andReturn();
        // Extract the News object from the model (works the same way as the way to get the model in the previous method )
        ModelAndView modelAndView = result.getModelAndView(); // get object ModelAndView from  MvcResult
        Map<String, Object> model = modelAndView.getModel(); // retrieves the model from ModelAndView. A model is a Map that contains attributes that have been added to the model by the controller.
        Project projectFromModel = (Project) model.get("project");

        Assertions.assertNotNull(projectFromModel);
        Assertions.assertEquals(project,projectFromModel);
    }

    @Test
    @WithMockUser(username = "username")
    public void MainController_Search_GetData() throws Exception {
        when(userService.findByUsername(user.getUsername())).thenReturn(user);
        when(projectService.search("projectName","allProjects")).thenReturn(Arrays.asList(project));

        MvcResult result = mockMvc.perform(get("/home/find")
                        .param("query","projectName")
                        .param("type","allProjects"))
                .andExpect(status().isOk())
                .andExpect(view().name("home-page :: allProjects"))
                .andReturn();

        ModelAndView modelAndViewContainer = result.getModelAndView();
        List<Project> projects = (List<Project>) modelAndViewContainer.getModel().get("allProjects");

        Assertions.assertNotNull(projects);
        Assertions.assertTrue(projects.size() == 1);
        Assertions.assertEquals(project,projects.get(0));
    }



}