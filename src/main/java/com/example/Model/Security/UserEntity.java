package com.example.Model.Security;

import com.example.Model.Chat;
import com.example.Model.Message;
import com.example.Model.Project;
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
@Table(name = "users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String email;
    private String password;
    private String town;
    private Long phoneNumber;
    private int roleId; //{0,1}
    String creationDate;

    @ToString.Exclude
    @ManyToMany(fetch = FetchType.EAGER , cascade = CascadeType.ALL)
    @JoinTable(
            name = "users_role",joinColumns = {@JoinColumn(name ="user_id",referencedColumnName ="id")},
            inverseJoinColumns ={@JoinColumn(name = "role_id", referencedColumnName = "id")}
    )// с помощью этой аннотации Spring сам создаст Join- таблицу
    private List<RoleEntity> roles = new ArrayList<>(); // список ролей для данного пользователя. Каждый пользователь может иметь список ролей.
    public boolean hasAdminRole() {
        if (roles == null) {
            return false;
        }
        for (RoleEntity role : roles) {
            if (role.getName().equals("ADMIN")) {
                return true;
            }
        }
        return false;
    }

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "users_projects",
            joinColumns = {@JoinColumn(name ="user_id",referencedColumnName ="id")},
            inverseJoinColumns ={@JoinColumn(name = "project_id", referencedColumnName = "id")}
    )
    private List<Project> currentProjects  = new ArrayList<>();

    // comments in Projects
    @JsonIgnore
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER, cascade = CascadeType.ALL , orphanRemoval = true)  // one user --> many comments in comment side i have @ ManyToone annotation
    private List<Message> messages = new ArrayList<>();
    // friends
    @ToString.Exclude
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "users_friends",
            joinColumns = {@JoinColumn(name ="user_id",referencedColumnName ="id")},
            inverseJoinColumns ={@JoinColumn(name = "friend_user_id", referencedColumnName = "id")}
    )
    private List<UserEntity> userFriends  = new ArrayList<>();
    // friends invitations
    @ToString.Exclude
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "friend_invitations",
            joinColumns = {@JoinColumn(name ="user_id",referencedColumnName ="id")},
            inverseJoinColumns ={@JoinColumn(name = "friend_user_id", referencedColumnName = "id")}
    )
    private List<UserEntity> userFriendsInvitations  = new ArrayList<>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "users_chats",
            joinColumns = {@JoinColumn(name ="user_id", referencedColumnName ="id")},
            inverseJoinColumns = {@JoinColumn(name = "chat_id", referencedColumnName = "id")}
    )
    private List<Chat> chats = new ArrayList<>();

}
