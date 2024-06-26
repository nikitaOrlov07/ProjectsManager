package com.example.Service.impl;

import com.example.Model.Chat;
import com.example.Model.Message;
import com.example.Model.Security.UserEntity;
import com.example.Repository.MessageRepository;
import com.example.Service.ChatService;
import com.example.Service.MessageService;
import com.example.Service.Security.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MessageServiceimpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private ChatService chatService;
    @Override
    public List<Message> findAllChatMessage(Long chaId) {
        return messageRepository.findAllByChatId(chaId);
    }
    @Override
    public Message saveMessage(Message message,Long chatId,UserEntity user) {

        Chat chat = chatService.findById(chatId).get();

        message.setChat(chat);

        message.setAuthor(user.getUsername());
        chatService.updateChat(chat);
        return  messageRepository.save(message);
    }
    @Override
    public Optional<Message> findById(Long message) {
        return messageRepository.findById(message);
    }
    @Override
    public void deleteMessage(Message message, UserEntity user , Chat chat) {
        user.getMessages().remove(message);
        chat.getMessages().remove(message);
        messageRepository.delete(message);
    }
    @Override
    public void deleteAllByChat(Chat chat)
    {
        messageRepository.deleteAllByChat(chat);
    }

    @Override
    public void delete(Message message) {
        messageRepository.delete(message);
    }


}
