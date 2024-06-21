package com.example.mappers;

import com.example.Dto.ProjectDto;
import com.example.Model.Project;

import java.time.format.DateTimeFormatter;

public class ProjectMapper {
    public static Project projectDtotoProject(ProjectDto projectDto)
    {
       String startDateString = null;String endDateString=null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if(projectDto != null && projectDto.getStartDate() != null)
            startDateString = projectDto.getStartDate().format(formatter);
        if(projectDto != null && projectDto.getEndDate() != null)
            endDateString = projectDto.getEndDate().format(formatter);

        Project project = Project.builder()
                .id(projectDto.getId())
                .name(projectDto.getName())
                .description(projectDto.getDescription())
                .category(projectDto.getCategory())
                .startDate(startDateString)
                .endDate(endDateString)
                .password(projectDto.getPassword())
                .chat(projectDto.getChat())
                .involvedUsers(projectDto.getInvolvedUsers())
                .build();
        return  project;
    }
}
