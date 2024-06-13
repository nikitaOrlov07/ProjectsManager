package com.example.Repository;

import com.example.Model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;
@Repository
public interface MessageRepository extends JpaRepository<Message,Long> {
List<Message> findAllByChatId(Long chatId);
}
