package com.example.Model;

import com.example.Model.Security.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "chats")
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ToString.Exclude
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "users_chats",
            joinColumns = {@JoinColumn(name = "chat_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")}
    )
    private List<UserEntity> participants = new ArrayList<>();
    @JsonIgnore
    @ToString.Exclude
    @OneToMany(mappedBy = "chat",fetch = FetchType.EAGER , cascade = CascadeType.ALL, orphanRemoval = true) // orphanRemoval child entities should be automatically deleted if they are no longer associated with the parent entity.
    private List<Message> messages = new ArrayList<>();
    @JsonIgnore
    @ToString.Exclude
    @OneToOne(mappedBy = "chat",fetch = FetchType.EAGER  , cascade = CascadeType.ALL)
    private Project project;

}
