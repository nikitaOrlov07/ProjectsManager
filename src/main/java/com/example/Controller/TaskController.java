package com.example.Controller;

import com.example.Dto.TaskDto;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Model.Task;
import com.example.Security.SecurityUtil;
import com.example.Service.ProjectService;
import com.example.Service.Security.UserService;
import com.example.Service.TaskService;
import com.example.mappers.TaskMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Controller
public class TaskController {
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

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
    // Ajax validation
    @PostMapping("/tasks/create")
    public ResponseEntity<String> ajaxCreateTask(@RequestBody TaskDto taskDto,
                                                 @RequestParam("projectId") Long projectId) {
        UserEntity user = userService.findByUsername(SecurityUtil.getSessionUser());
        Project project = projectService.findById(projectId);

        if (user == null || (!project.getInvolvedUsers().contains(user))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access");
        }

        Task task = TaskMapper.getTaskFromTaskDto(taskDto);
        task.setProject(project);
        taskService.save(task);
        logger.info("Task created");
        return ResponseEntity.ok("success");
    }
    @PostMapping("projects/{projectId}/tasks/reset")
    public String resetTasks(@PathVariable("projectId") Long projectId)
    {
        Project project = projectService.findById(projectId);
        List<Task> tasks = project.getTasks();
        taskService.delete(tasks);
        project.getTasks().clear(); // clear tasks list from project
        projectService.save(project);
        return "redirect:/projects/"+projectId;
    }


}
