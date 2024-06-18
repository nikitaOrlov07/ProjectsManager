package com.example.Service;


import com.example.Model.Project;
import com.example.Model.Task;

import java.util.List;

public interface TaskService {
    Task findById(Long taskId);

    void save(Task task);

    void delete(List<Task> tasks);
}
