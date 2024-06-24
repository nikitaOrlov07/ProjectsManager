package com.example.Service.Security;

import com.example.Dto.security.RegistrationDto;
import com.example.Model.Attachment;
import com.example.Model.Chat;
import com.example.Model.Message;
import com.example.Model.Project;
import com.example.Model.Security.RoleEntity;
import com.example.Model.Security.UserEntity;
import com.example.Repository.AttachmentRepository;
import com.example.Repository.MessageRepository;
import com.example.Repository.Security.RoleRepository;
import com.example.Repository.Security.UserRepository;
import com.example.Service.ChatService;
import com.example.Service.ProjectService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceimpl implements UserService{
    private UserRepository userRepository; private RoleRepository roleRepository;  // implements methods from repositories
    private PasswordEncoder passwordEncoder;  private MessageRepository messageRepository; private AttachmentRepository attachmentRepository; private ChatService chatService;
    @Autowired
    public UserServiceimpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, MessageRepository messageRepository ,  AttachmentRepository attachmentRepository,ChatService chatService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.attachmentRepository = attachmentRepository;
        this.messageRepository = messageRepository;
        this.chatService=chatService;
    }

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
    @Transactional
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
                currentUser.getUserFriendsInvitations().remove(friendUser);
                friendUser.getUserFriends().remove(currentUser);
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
            case "invitations":
                return userRepository.searchInvitations(query);
            default:
                return null;
        }
    }

    @Override
    public void save(UserEntity currentUser) {
        userRepository.save(currentUser);
    }

    @Override
    public void delete(UserEntity deletedUser) {
        // Delete user from projects
        List<Project> projects = new ArrayList<>(deletedUser.getCurrentProjects());
        for (Project project : projects) {
            project.getInvolvedUsers().remove(deletedUser);
        }

        // Delete user from his friends
        List<UserEntity> friends = new ArrayList<>(deletedUser.getUserFriends());
        for (UserEntity friend : friends) {
            friend.getUserFriends().remove(deletedUser);
        }

        // Delete all attachments
        List<Attachment> attachments = attachmentRepository.findAllByCreator(deletedUser.getUsername());
        for (Attachment attachment : attachments) {
            attachmentRepository.delete(attachment);
        }

        // Delete user chats
        List<Chat> chats = chatService.findAllByParticipants(deletedUser);
        for (Chat chat : chats) {
            chat.getParticipants().remove(deletedUser);
            List<Message> messages = new ArrayList<>(chat.getMessages());
            for (Message message : messages) {
                if (message.getUser().equals(deletedUser)) {
                    chat.getMessages().remove(message);
                }
            }
            if (chat.getParticipants().isEmpty()) {
                chatService.delete(chat);
            }
        }


        // Удаляем пользователя из репозитория
        userRepository.delete(deletedUser);
    }


    @Override
    public List<UserEntity> findAllUsers() {
        return userRepository.findAll();
    }






}
