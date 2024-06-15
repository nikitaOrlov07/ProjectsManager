package com.example.Service.impl;

import com.example.Model.Project;
import com.example.Model.Task;
import com.example.Repository.TaskRepository;
import com.example.Service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskServiceimpl implements TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Override
    public Task findById(Long taskId) {
        return taskRepository.findById(taskId).get();
    }

    @Override
    public void save(Task task) {
        taskRepository.save(task);
    }

}
