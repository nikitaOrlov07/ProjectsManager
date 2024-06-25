package com.example.projectmanager.Services;

import com.example.Dto.ProjectDto;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Repository.ProjectRepository;
import com.example.Repository.Security.UserRepository;
import com.example.Service.TaskService;
import com.example.Service.impl.ProjectServiceimpl;
import com.example.mappers.ProjectMapper;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ProjectServiceTest {

   @Mock
    private ProjectRepository projectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskService taskService;

    @InjectMocks
    private ProjectServiceimpl projectService;

    private UserEntity user;

    @BeforeEach
    public void setUp() {
        user = UserEntity.builder()
                .username("username")
                .email("email")
                .password("password")
                .build();
    }

    @Test
    public void ProjectService_CreateProject_ReturnsProjectDto() {
        Project project = Project.builder()
                .name("project")
                .description("project description")
                .category("project category")
                .build();

        ProjectDto projectDto = ProjectDto.builder()
                .name("project")
                .description("project description")
                .category("project category")
                .build();

        when(projectRepository.save(Mockito.any(Project.class))).thenReturn(project);

        ProjectDto savedProject = projectService.createProject(projectDto, user);

        Assertions.assertNotNull(savedProject);
        Assertions.assertEquals(project.getName(), savedProject.getName());
        Assertions.assertEquals(project.getDescription(), savedProject.getDescription());
        Assertions.assertEquals(project.getCategory(), savedProject.getCategory());

        verify(projectRepository).save(Mockito.any(Project.class));
    }
}