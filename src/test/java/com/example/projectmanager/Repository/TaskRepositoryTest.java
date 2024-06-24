package com.example.projectmanager.Repository;

import com.example.Model.Task;
import com.example.Repository.TaskRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2) // configure H2 database
public class TaskRepositoryTest {
    @Autowired
    private TaskRepository taskRepository;

    @Test
    public void TaskRepository_saveTask_ReturnSavedTask()
    {
        Task task = Task.builder()
                .name("name")
                .description("description")
                .complete(false)
                .build();
        Task savedTask = taskRepository.save(task);
        Assertions.assertNotNull(savedTask);
        Assertions.assertEquals(task,savedTask);
    }

    @Test
    public void TaskRepository_findByIdTask_ReturnSavedTask()
    {
        Task task = Task.builder()
                .name("name")
                .description("description")
                .complete(false)
                .build();
        taskRepository.save(task);
        Task returnedTask = taskRepository.findById(task.getId()).get();
        Assertions.assertNotNull(returnedTask);
        Assertions.assertEquals(task,returnedTask);
    }

}
