package com.example.projectmanager.Repository;

import com.example.Model.Project;
import com.example.Repository.ProjectRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // configure H2 database
public class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    public void ProjectRepository_save_ReturnSavedProject() {
        Project project = Project.builder()
                .name("project")
                .description("description")
                .category("category")
                .startDate("25-50-30")
                .endDate("25-50-30")
                .build();
        Project savedProject = projectRepository.save(project);

        Assertions.assertNotNull(savedProject);
        Assertions.assertEquals(project.getName(), savedProject.getName());
        Assertions.assertEquals(project.getDescription(), savedProject.getDescription());
        Assertions.assertEquals(project.getCategory(), savedProject.getCategory());
        Assertions.assertEquals(project.getStartDate(), savedProject.getStartDate());
        Assertions.assertEquals(project.getEndDate(), savedProject.getEndDate());
    }

    @Test
    public void ProjectRepository_findAll_ReturnSavedProjects() {
        Project project1 = Project.builder()
                .name("project1")
                .description("description1")
                .category("category1")
                .startDate("25-50-30")
                .endDate("25-50-30")
                .build();

        Project project2 = Project.builder()
                .name("project2")
                .description("description2")
                .category("category2")
                .startDate("25-50-30")
                .endDate("25-50-30")
                .build();

        projectRepository.save(project1);
        projectRepository.save(project2);
        List<Project> projects = projectRepository.findAll();

        Assertions.assertNotNull(projects);
        Assertions.assertEquals(2, projects.size(), "Must be two projects");
    }

    @Test
    public void ProjectRepository_findById_ReturnSavedProject() {
        Project project = Project.builder()
                .name("project")
                .description("description")
                .category("category")
                .startDate("25-50-30")
                .endDate("25-50-30")
                .build();

        Project savedProject = projectRepository.save(project);
        Project returnedProject = projectRepository.findById(savedProject.getId()).get();

        Assertions.assertNotNull(returnedProject);
        Assertions.assertEquals(project.getName(), returnedProject.getName());
        Assertions.assertEquals(project.getDescription(), returnedProject.getDescription());
        Assertions.assertEquals(project.getCategory(), returnedProject.getCategory());
        Assertions.assertEquals(project.getStartDate(), returnedProject.getStartDate());
        Assertions.assertEquals(project.getEndDate(), returnedProject.getEndDate());
    }

    @Test
    public void ProjectRepository_findByTitleAllProjects_ReturnProjects() {
        Project project = Project.builder()
                .name("project")
                .description("description")
                .category("category")
                .startDate("25-50-30")
                .endDate("25-50-30")
                .build();

        projectRepository.save(project);
        List<Project> returnedProjects = projectRepository.searchAllProjects(project.getName());

        Assertions.assertNotNull(returnedProjects);
        Assertions.assertFalse(returnedProjects.isEmpty());
        Project returnedProject = returnedProjects.get(0);
        Assertions.assertEquals(project, returnedProject);

    }
    @Test
    public void ProjectRepository_DeleteProject_ReturnNull()
    {
        Project project = Project.builder()
                .name("project")
                .description("description")
                .category("category")
                .startDate("25-50-30")
                .endDate("25-50-30")
                .build();

        projectRepository.save(project);
        projectRepository.delete(project);
        Optional<Project> returnedProject = projectRepository.findById(project.getId());

        Assertions.assertTrue(returnedProject.isEmpty());

    }
}
