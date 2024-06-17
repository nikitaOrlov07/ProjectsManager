package com.example.Controller;

import com.example.Dto.TaskDto;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Model.Task;
import com.example.Security.SecurityUtil;
import com.example.Service.ProjectService;
import com.example.Service.Security.UserService;
import com.example.Service.TaskService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    @PostMapping("/ajax/tasks/create")
    public String ajaxCreateTask(@Valid @ModelAttribute("taskDto") TaskDto taskDto,
                                 BindingResult result,
                                 @RequestParam("projectId") Long projectId,
                                 Model model) {
        if (result.hasErrors() || taskDto.getName().isEmpty() || taskDto.getDescription().isEmpty()) {
            System.out.println("ВАЛИДАЦИЯ СРАБАТЫВАЕТ");
            model.addAttribute("taskDto", taskDto);
            model.addAttribute("project", projectService.findById(projectId));
            return "detail-page :: addTask";
        }

        // Логика создания задачи
        taskDto.setProject(projectService.findById(projectId));
        // Сохранение задачи...

        return "success";
    }
}
