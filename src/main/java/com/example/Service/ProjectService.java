package com.example.Service;

import com.example.Dto.ProjectDto;
import com.example.Model.Chat;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;

import java.util.List;

public interface ProjectService {
    List<Project> findAllProjects();

    List<Project> findUsersProjects();

    List<Project> search(String query,String type);

    Project findById(Long projectId);
    void save (Project project);

    void delete(Project project);

    ProjectDto createProject(ProjectDto projectDto, UserEntity user);

    Project findProjectByChat(Chat chat);
    Long getRemainingDays(String endDate);
}
