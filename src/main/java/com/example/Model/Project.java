package com.example.Model;

import com.example.Model.Security.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
    private  String password;
    @ToString.Exclude
    @JsonIgnore
    @ManyToMany(mappedBy = "currentProjects", fetch = FetchType.EAGER)
    private List<UserEntity> involvedUsers = new ArrayList<>();

    @ToString.Exclude
    @JsonManagedReference // to eliminate recursion
    @OneToMany(mappedBy = "project",fetch = FetchType.EAGER,cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    @JsonIgnore
    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "chat_id", referencedColumnName = "id")
    private Chat chat;

    @OneToMany(mappedBy = "project",fetch = FetchType.EAGER ,cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonManagedReference // to eliminate recursion
    private List<Attachment> attachments = new ArrayList<>();

}
