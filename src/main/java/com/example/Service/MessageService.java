package com.example.Service;

import com.example.Model.Chat;
import com.example.Model.Message;
import com.example.Model.Security.UserEntity;

import java.util.List;
import java.util.Optional;

public interface MessageService {
    List<Message> findAllChatMessage(Long chaId);
    Message saveMessage(Message comment,Long chatId, UserEntity user);

    Optional<Message> findById(Long messageId);

    void deleteMessage(Message message, UserEntity user , Chat chat);
}
