package com.example.projectmanager.Service;

import com.example.Model.Project;
import com.example.Model.Task;
import com.example.Repository.TaskRepository;
import com.example.Service.impl.TaskServiceimpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {
    @Mock
    private TaskRepository taskRepository;
    @InjectMocks
    private TaskServiceimpl taskService;
    private Task task; private Project project;

    @BeforeEach
    private void setUp()
    {
        task = Task.builder()
                .id(1L)
                .complete(false)
                .name("taskName")
                .description("taskDescription")
                .project(project)
                .build();
    }
    @Test
    public void TaskService_findById_returnTask()
    {

        when(taskRepository.findById(task.getId())).thenReturn(Optional.ofNullable(task));
        Task returnedTask = taskService.findById(task.getId());

        Assertions.assertNotNull(returnedTask);
        Assertions.assertEquals(task,returnedTask);
    }
    @Test
    public void TaskService_saveTask_returnTask()
    {
      when(taskRepository.save(task)).thenReturn(task);

      Task savedTask = taskService.save(task);

      Assertions.assertNotNull(savedTask);
      Assertions.assertTrue(task.equals(savedTask));
    }

    @Test
    public void TaskService_deleteTasks_deleteAllCalled()
    {
        // Arrange
        List<Task> tasks = Arrays.asList(task);
        doNothing().when(taskRepository).deleteAll();

        // Act
        taskService.delete(tasks);

        // Assert
        Mockito.verify(taskRepository, times(1)).deleteAll();
    }

}
