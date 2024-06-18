package com.example.Repository;

import com.example.Model.Project;
import com.example.Model.Security.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProjectRepository  extends JpaRepository<Project,Long> {
    List<Project> findAllByInvolvedUsersContains(UserEntity user);
    // For Searching
    @Query("Select c from Project c WHERE c.name LIKE CONCAT('%', :query ,'%')")
    List<Project> searchAllProjects(String query);
    @Query("SELECT p FROM UserEntity u JOIN u.currentProjects p WHERE p.name LIKE CONCAT('%', :query, '%')")
    List<Project> searchUserProjects(@Param("query") String query);

}
