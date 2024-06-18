package com.example.Service.impl;

import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Repository.ProjectRepository;
import com.example.Repository.Security.UserRepository;
import com.example.Security.SecurityUtil;
import com.example.Service.ProjectService;
import com.example.Service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Security;
import java.util.*;

@Service
public class ProjectServiceimpl implements ProjectService {
    private ProjectRepository projectRepository;
    private UserRepository userRepository;
    private TaskService taskService;

    @Autowired
    public ProjectServiceimpl(ProjectRepository projectRepository, UserRepository userRepository,TaskService taskService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.taskService = taskService;
    }

    public List<Project> findAllProjects() {
        return projectRepository.findAll();
    }

    @Override
    public List<Project> findUsersProjects() {
        UserEntity user = userRepository.findByUsername(SecurityUtil.getSessionUser());
        return projectRepository.findAllByInvolvedUsersContains(user);
    }

    @Override
    public List<Project> search(String query, String type) {
        if (type.equals("allProjects")) {
            return projectRepository.searchAllProjects(query);
        } else if (type.equals("userProjects")) {
            return projectRepository.searchUserProjects(query);
        }
        return null;
    }

    @Override
    public Project findById(Long projectId) {
        return projectRepository.findById(projectId).get();
    }

    @Override
    public void save(Project project) {
        projectRepository.save(project);
    }

    @Override
    public void delete(Project project) {
          List<UserEntity> users = project.getInvolvedUsers();
          users.forEach(user -> {user.getCurrentProjects().remove(project);});
          taskService.delete(project.getTasks());
          projectRepository.delete(project);
    }
}
