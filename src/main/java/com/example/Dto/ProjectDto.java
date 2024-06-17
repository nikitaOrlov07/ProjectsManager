package com.example.Dto;

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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Valid
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
    private String startDate;
    @Future(message="This date has already passed")
    private String endDate;
    @Size(min = 3 , message = "The minimum length of the password must be 3 characters")
    private String password;
}
