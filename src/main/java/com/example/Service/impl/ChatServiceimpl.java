package com.example.Service.impl;

import com.example.Model.Chat;
import com.example.Model.Message;
import com.example.Model.Security.UserEntity;
import com.example.Repository.ChatRepository;
import com.example.Repository.MessageRepository;
import com.example.Service.ChatService;
import com.example.Service.MessageService;
import com.example.Service.Security.UserService;
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
    @Autowired
    private MessageRepository messageRepository;
    @Lazy
    @Autowired
    private MessageService messageService;
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
        newChat.getParticipants().add(currentUser);
        newChat.getParticipants().add(secondUser);
        chatRepository.save(newChat);
        log.info("was returned new chat");
        return newChat;
    }

    @Transactional
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
        List<Message> messages = messageRepository.findAllByChatId(chat.getId());
        for(Message message : messages) {
            messageService.deleteMessage(message,message.getUser(),chat);
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
