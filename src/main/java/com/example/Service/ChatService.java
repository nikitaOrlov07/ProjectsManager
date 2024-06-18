package com.example.Service;

import com.example.Model.Chat;
import com.example.Model.Security.UserEntity;

import java.util.Optional;

public interface ChatService {

    Optional<Chat> findById(Long chatId);

    void updateChat(Chat chat);

    Chat findOrCreateChat(UserEntity currentUser, UserEntity secondUser);

    void save(Chat chat);
}
