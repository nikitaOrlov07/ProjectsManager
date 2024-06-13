package com.example.Model;

import com.example.Model.Security.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String category;
    private String startDate;
    private String endDate;

    @ToString.Exclude
    @ManyToMany(mappedBy = "currentProjects")
    private List<UserEntity> involvedUsers = new ArrayList<>();




}
