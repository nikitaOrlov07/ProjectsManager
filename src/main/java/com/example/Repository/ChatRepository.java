package com.example.Repository;

import com.example.Model.Chat;
import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {

    List<Chat> findByParticipantsContains(UserEntity user);


}
