package com.example.Model;

import com.example.Model.Security.UserEntity;
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
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "users_chats",
            joinColumns = {@JoinColumn(name = "chat_id", referencedColumnName = "id")},
            inverseJoinColumns = {@JoinColumn(name = "user_id", referencedColumnName = "id")}
    )
    private List<UserEntity> participants = new ArrayList<>();
    @ToString.Exclude
    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true) // orphanRemoval child entities should be automatically deleted if they are no longer associated with the parent entity.
    private List<Message> messages = new ArrayList<>();
    @ToString.Exclude
    @OneToOne(mappedBy = "chat", cascade = CascadeType.ALL)
    private Project project;

}
