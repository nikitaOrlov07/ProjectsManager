package com.example.Service.impl;

import com.example.Model.Chat;
import com.example.Model.Message;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import com.example.Repository.ChatRepository;
import com.example.Repository.MessageRepository;
import com.example.Service.ChatService;
import com.example.Service.MessageService;
import com.example.Service.ProjectService;
import com.example.Service.Security.UserService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class ChatServiceimpl implements ChatService {
    @Autowired
    private ChatRepository chatRepository;
    @Lazy // Create a bean only when program will need it.
    @Autowired
    private UserService userService;
    @Lazy
    @Autowired
    private ProjectService projectService;
    @Lazy
    @Autowired
    private MessageService messageService;

    @Autowired
    private MessageRepository messageRepository;

    @Transactional
    @Override
    public Optional<Chat> findById(Long chatId) {
        return chatRepository.findById(chatId);
    }

    @Override
    public void updateChat(Chat chat) {
        chatRepository.save(chat);
    }

    @Transactional
    @Override
    public Chat findOrCreateChat(UserEntity currentUser, UserEntity secondUser) {
        // Reboot users to make sure they are in the "managed" state (not detached)
        currentUser = userService.findById(currentUser.getId());
        secondUser = userService.findById(secondUser.getId());

        List<Chat> existingChats = chatRepository.findByParticipantsContains(currentUser);

        for (Chat chat : existingChats) {
            if (chat.getParticipants().contains(secondUser)) {
              log.info("was returned existing chat");
                return chat;
            }
        }

        Chat newChat = new Chat();
        newChat.addParticipant(currentUser);
        newChat.addParticipant(secondUser);



        // create new chat
        chatRepository.save(newChat);
        if((currentUser.getChats().contains(newChat) && secondUser.getChats().contains(newChat)) &&  (newChat.getParticipants().contains(currentUser) && (newChat.getParticipants().contains(secondUser))))
        {
            log.info("users were added to chat participants");
        }
        else
            log.error("users were not added to chat participants");

        log.info("was returned new chat");
        return newChat;
    }

    @Transactional
    @Override
    public void save(Chat chat) {
        chatRepository.save(chat);
    }

    @Transactional
    @Override
    public void delete(Chat chat) {
        List<UserEntity> participants = new ArrayList<>(chat.getParticipants());
        for (UserEntity user : participants) {
            user.getChats().remove(chat);
            chat.getParticipants().remove(user);
        }

        List<Message> messagesToDelete = new ArrayList<>(chat.getMessages()); // create a copy of message list and then we can  iterate  and safely modify the original collection
        for (Message message : messagesToDelete) {
            messageService.deleteMessage(message, message.getUser(), chat);
        }

        chat.getMessages().clear();
        chatRepository.delete(chat);
    }
    @Override
    public List<Chat> findAllByParticipants(UserEntity deletedUser) {
        return chatRepository.findByParticipantsContains(deletedUser);
    }

    @Transactional // In a @Transactional method, Hibernate automatically tracks changes to managed entities and persists them at the end of the transaction.
    @Override
    public void clearMessages(Chat chat) {
        Chat managedChat = chatRepository.findById(chat.getId())
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        List<Message> messages = new ArrayList<>(managedChat.getMessages());
        for (Message message : messages) {
            message.setUser(null);
            message.setChat(null);
            managedChat.getMessages().remove(message);
            messageRepository.delete(message);
        }

        managedChat.getMessages().clear();

        Project project = managedChat.getProject();
        if (project != null) {
            project.setChat(managedChat);
            projectService.save(project);
        }
    }
}
