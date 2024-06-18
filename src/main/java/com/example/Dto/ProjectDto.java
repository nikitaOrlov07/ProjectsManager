package com.example.Dto;

import com.example.Model.Chat;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotEmpty(message = "You must enter a project title")
    private String name;
    @NotEmpty(message = "You must enter a project description")
    private String description;
    @NotEmpty(message = "You must enter a project category")
    private String category;
    @Future(message ="This date has already passed")
    private LocalDate startDate;
    @Future(message="This date has already passed")
    private LocalDate endDate;
    private String password;
    private Chat chat;
}
