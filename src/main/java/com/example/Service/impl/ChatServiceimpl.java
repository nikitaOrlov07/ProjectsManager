package com.example.Service.impl;

import com.example.Model.Chat;
import com.example.Model.Security.UserEntity;
import com.example.Repository.ChatRepository;
import com.example.Service.ChatService;
import com.example.Service.Security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChatServiceimpl implements ChatService {
    @Autowired
    private ChatRepository chatRepository;
    @Autowired
    private UserService userService;
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
}
