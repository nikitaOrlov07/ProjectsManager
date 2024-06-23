package com.example.Repository.Security;

import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<UserEntity,Long> {
    UserEntity findFirstByUsername(String username);
    UserEntity findByUsername(String username);

    UserEntity findByEmail(String email);

    // for searching
    @Query("SELECT u FROM UserEntity u WHERE u.username LIKE CONCAT('%', :query ,'%')") // select from entity name , not from table name
    List<UserEntity> searchAllUsers(@Param("query") String query);
    @Query("SELECT u FROM UserEntity u JOIN u.userFriends  WHERE u.username LIKE CONCAT('%', :query, '%')")
    List<UserEntity> searchUserFriends(@Param("query") String query);

    @Query("SELECT p FROM UserEntity u JOIN u.userFriendsInvitations p WHERE p.username LIKE CONCAT('%', :query, '%')")
    List<UserEntity> searchInvitations(@Param("query") String query);

}
