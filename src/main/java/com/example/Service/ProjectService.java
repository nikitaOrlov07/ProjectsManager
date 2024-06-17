package com.example.Service;

import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public interface ProjectService {
    List<Project> findAllProjects();

    List<Project> findUsersProjects();

    List<Project> search(String query,String type);

    Project findById(Long projectId);
    void save (Project project);

}
