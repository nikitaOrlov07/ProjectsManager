package com.example.Controller;

import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Model.Task;
import com.example.Security.SecurityUtil;
import com.example.Service.ProjectService;
import com.example.Service.Security.UserService;
import com.example.Service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class TaskController {
    private UserService userService; private ProjectService projectService; private TaskService taskService;
    @Autowired
    public TaskController(UserService userService, ProjectService projectService, TaskService taskService) {
        this.userService = userService;
        this.projectService = projectService;
        this.taskService = taskService;
    }
    @PostMapping("/taskComplete/{taskId}")
    public String completeTask(@PathVariable("taskId") Long taskId, @RequestParam("complete") boolean complete) {
        UserEntity user = userService.findByUsername(SecurityUtil.getSessionUser());
        Task task = taskService.findById(taskId);
        if(user == null || !task.getProject().getInvolvedUsers().contains(user))
        {return "redirect:/home";}
        System.out.println("Task name: " + task.getName() + " Current Task status: " + task.isComplete());
        task.setComplete(complete); // Устанавливаем значение, переданное с клиента
        taskService.save(task); // Сохраняем задачу
        System.out.println("After change: Task name: " + task.getName() + " New Task status: " + task.isComplete());
        return "redirect:/projects/" + task.getProject().getId(); // Перенаправление на страницу проекта
    }



}
