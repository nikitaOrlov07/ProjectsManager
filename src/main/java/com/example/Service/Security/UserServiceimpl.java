package com.example.Service.Security;

import com.example.Dto.security.RegistrationDto;
import com.example.Model.Project;
import com.example.Model.Security.RoleEntity;
import com.example.Model.Security.UserEntity;
import com.example.Repository.MessageRepository;
import com.example.Repository.Security.RoleRepository;
import com.example.Repository.Security.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceimpl implements UserService{
    private UserRepository userRepository; private RoleRepository roleRepository;  // implements methods from repositories
    private PasswordEncoder passwordEncoder;  private MessageRepository messageRepository;
    @Autowired
    public UserServiceimpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, MessageRepository messageRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;

        this.messageRepository = messageRepository;
    } // Technically we don`t need a constructor , but it is good practice

    @Override
    public void saveUser(RegistrationDto registrationDto) {
        UserEntity userEntity = new UserEntity(); // we cant save RegistrationDto to the database because it`s totally different entity
        // create something like mapper
        userEntity.setUsername(registrationDto.getUsername());
        userEntity.setEmail(registrationDto.getEmail());
        userEntity.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        userEntity.setTown(registrationDto.getTown());
        userEntity.setPhoneNumber(registrationDto.getPhoneNumber());

        RoleEntity role = roleRepository.findByName("USER");// по факту "USER"  записывается в переменную role (- В этой строке мы ищем объект RoleEntity, представляющий роль "USER" в системе.)
        userEntity.setRoles(Arrays.asList(role));// даем нашему userEntity (юзеру) роль "USER" (мы назначаем найденную роль "USER" пользователю, устанавливая список ролей пользователя в качестве списка,
        // содержащего только одну роль "USER".)
        //----------------------------------------------------------------
        userRepository.save(userEntity);
    }

    @Override
    public UserEntity findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserEntity findByUsername(String username) {
       return  userRepository.findByUsername(username);
    }

    @Override
    public UserEntity findById(Long userId) {
        Optional<UserEntity> optionalUser = userRepository.findById(userId);
        return optionalUser.orElse(null);
    }
    @Override
    public List<Project> findCommonProjects(UserEntity user, UserEntity friend) {
        List<Project> userProjects = user.getCurrentProjects();
        List<Project> friendProjects = friend.getCurrentProjects();

        return userProjects.stream()
                .filter(friendProjects::contains)
                .collect(Collectors.toList()); // create stream of current user projects -> then take only those projects ,that a friend also has and return them to controller
    }

    @Override
    public void friendsLogic(UserEntity currentUser, UserEntity friendUser, String type) {
        switch (type) {
            case "add":
                currentUser.getUserFriends().add(friendUser);
                friendUser.getUserFriends().add(currentUser);
                break;
            case "remove":
                currentUser.getUserFriends().remove(friendUser);
                friendUser.getUserFriends().remove(currentUser);
                break;
        }
        userRepository.save(currentUser); userRepository.save(friendUser);
    }

    @Override
    public List<UserEntity> search(String query, String type) {
        switch (type) {
            case "allUsers":
                return userRepository.searchAllUsers(query);

            case "userFriends":
                return userRepository.searchUserFriends(query);
            default:
                return null;
        }
    }

    @Override
    public void save(UserEntity currentUser) {
        userRepository.save(currentUser);
    }


    @Override
    public List<UserEntity> findAllUsers() {
        return userRepository.findAll();
    }






}
