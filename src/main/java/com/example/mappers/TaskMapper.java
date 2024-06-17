package com.example.mappers;

import com.example.Dto.TaskDto;
import com.example.Model.Task;

public class TaskMapper {
    static Task getTaskFromTaskDto(TaskDto taskDto)
    {
        Task task = Task.builder()
                .name(taskDto.getName())
                .description(taskDto.getDescription())
                .complete(taskDto.isComplete())
                .project(taskDto.getProject())
                .build();
        return task;
    }
    static TaskDto getTaskDtoFromTask(Task task)
    {
        TaskDto taskDto = TaskDto.builder()
                .name(task.getName())
                .description(task.getDescription())
                .complete(task.isComplete())
                .project(task.getProject())
                .build();
        return taskDto;
    }
}
