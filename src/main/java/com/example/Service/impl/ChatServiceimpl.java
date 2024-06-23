package com.example.Service.impl;

import com.example.Model.Chat;
import com.example.Model.Message;
import com.example.Model.Security.UserEntity;
import com.example.Repository.ChatRepository;
import com.example.Repository.MessageRepository;
import com.example.Service.ChatService;
import com.example.Service.Security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ChatServiceimpl implements ChatService {
    @Autowired
    private ChatRepository chatRepository;
    @Lazy // Create a bean only when program will need it.
    @Autowired
    private UserService userService;
    @Autowired
    private MessageRepository messageRepository;
    @Override
    public Optional<Chat> findById(Long chatId) {
        return chatRepository.findById(chatId);
    }

    @Override
    public void updateChat(Chat chat) {
        chatRepository.save(chat);
    }

    @Override
    public Chat findOrCreateChat(UserEntity currentUser, UserEntity secondUser) {
        List<Chat> existingChats = chatRepository.findByParticipantsContains(currentUser);

        for (Chat chat : existingChats) {
            if (chat.getParticipants().contains(secondUser)) {
                System.out.println("Was returned existed chat ");
                return chat; // if participants have already had chat -> it will return
            }
        }

        Chat newChat = new Chat(); // if participants have not already had chat -> will create a new chat
        newChat.getParticipants().add(currentUser);
        newChat.getParticipants().add(secondUser);
        chatRepository.save(newChat);
        System.out.println("was returned new chat");
        return newChat;
    }

    @Override
    public void save(Chat chat) {
        chatRepository.save(chat);
    }

    @Override
    public void delete(Chat chat) {
        List<UserEntity> participants = new ArrayList<>(chat.getParticipants());
        for(UserEntity user : participants) {
            user.getChats().remove(chat);
            chat.getParticipants().remove(user);
        }
        chatRepository.delete(chat);
    }
    @Override
    public List<Chat> findAllByParticipants(UserEntity deletedUser) {
        return chatRepository.findByParticipantsContains(deletedUser);
    }

    @Override
    @Transactional
    public void clearMessages(Chat chat) {
        List<Message> messages = new ArrayList<>(chat.getMessages());
        for(Message message : messages) {
            UserEntity user = message.getUser();
            if (user != null) {
                user.getMessages().remove(message);
            }
            chat.getMessages().remove(message);
            messageRepository.delete(message);
        }
        chatRepository.save(chat);
    }
}
