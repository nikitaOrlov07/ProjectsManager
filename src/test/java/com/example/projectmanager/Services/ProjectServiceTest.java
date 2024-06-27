package com.example.projectmanager.Services;

import com.example.Dto.ProjectDto;
import com.example.Model.Chat;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Repository.ProjectRepository;
import com.example.Repository.Security.UserRepository;
import com.example.Security.SecurityUtil;
import com.example.Service.TaskService;
import com.example.Service.impl.ProjectServiceimpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;

import java.security.Security;
import java.util.*;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskService taskService;
    @Mock
    private SecurityUtil securityUtil;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private ProjectServiceimpl projectService;

    private UserEntity user;
    private Project project;

    @BeforeEach
    public void setUp() {
        project = Project.builder()
                .id(1L)
                .name("project")
                .description("project description")
                .category("project category")
                .build();

        user = UserEntity.builder()
                .username("username")
                .email("email")
                .password("password")
                .roles(new ArrayList<>())
                .currentProjects(new ArrayList<>())
                .messages(new ArrayList<>())
                .userFriends(new ArrayList<>())
                .userFriendsInvitations(new ArrayList<>())
                .chats(new ArrayList<>())
                .build();
    }

    @Test
    public void ProjectService_CreateProject_ReturnsProjectDto() {
        // Arrange
        ProjectDto projectDto = ProjectDto.builder()
                .name("project")
                .description("project description")
                .category("project category")
                .build();


        when(projectRepository.save(Mockito.any(Project.class))).thenReturn(project);

        // Act
        ProjectDto savedProject = projectService.createProject(projectDto, user);

        // Assert
        Assertions.assertNotNull(savedProject);
        Assertions.assertEquals(project.getName(), savedProject.getName());
        Assertions.assertEquals(project.getDescription(), savedProject.getDescription());
        Assertions.assertEquals(project.getCategory(), savedProject.getCategory());
        verify(projectRepository, times(2)).save(Mockito.any(Project.class)); // Verify that save was called twice in "crreateProject" service method
    }
    @Test
    public void ProjectService_GetAllProjects_ReturnsProject() {
     projectRepository.save(project);
     when(projectRepository.findAll()).thenReturn(Arrays.asList(project));

     List<Project> projects = projectService.findAllProjects();

     Assertions.assertNotNull(projects);
     Assertions.assertEquals(project,projects.get(0));
    }
    @Test
    public void ProjectService_DeleteProject_DeleteProject() {
        // Arrange
        project.setInvolvedUsers(Arrays.asList(user));
        projectRepository.save(project);
        // Act
        projectService.delete(project);
        // Assert
        verify(projectRepository).delete(project);
    }
    @Test
    public void ProjectService_FindProjectByChat_returnProject()
    {
        // Arrange
        projectRepository.save(project);

        Chat chat = Chat.builder()
                .project(project)
                .build();
        // Act
        when(projectRepository.findProjectByChat(Mockito.any(Chat.class))).thenReturn(project);
        Project returnedProject = projectService.findProjectByChat(chat);

        // Assert
        Assertions.assertNotNull(project);
        Assertions.assertEquals(project,returnedProject);
    }
    @Test
    public void ProjectService_searchProject_returnProject()
    {
        // Arrange
        Project project2 = Project.builder()
                .id(2L)
                .name("project2")
                .description("project2 description")
                .category("project2 category")
                .build();
        projectRepository.save(project); projectRepository.save(project2);
        // Act
        when(projectRepository.searchAllProjects("project2")).thenReturn(Arrays.asList(project2));
        List<Project> returnedProjects = projectService.search("project2","allProjects");

        // Assert
        Assertions.assertNotNull(returnedProjects);
        Assertions.assertEquals(project2,returnedProjects.get(0));
    }

}
