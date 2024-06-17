package com.example.mappers;

import com.example.Dto.ProjectDto;
import com.example.Model.Project;

public class ProjectMapper {
    public Project projectDtotoProject(ProjectDto projectDto)
    {
        Project project = Project.builder()
                .id(projectDto.getId())
                .name(projectDto.getName())
                .description(projectDto.getDescription())
                .category(projectDto.getCategory())
                .startDate(projectDto.getStartDate())
                .endDate(projectDto.getEndDate())
                .password(projectDto.getPassword())
                .build();
        return  project;
    }
}
