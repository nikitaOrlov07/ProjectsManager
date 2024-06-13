package com.example.Service.Security;



import com.example.Dto.security.RegistrationDto;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;

import javax.management.Query;
import java.util.List;

public interface UserService {
    void saveUser(RegistrationDto registrationDto);

    UserEntity findByEmail(String email);

    UserEntity findByUsername(String username);

    List<UserEntity> findAllUsers();

    UserEntity findById(Long userId);

    List<Project> findCommonProjects(UserEntity user, UserEntity friend);

    void friendsLogic(UserEntity currentUser, UserEntity friendUser, String type);

    List<UserEntity> search(String query, String type);

    void save(UserEntity currentUser);
}
