package com.livesend.repository;

import com.livesend.entity.SendTask;
import com.livesend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SendTaskRepository extends JpaRepository<SendTask, Long> {
    List<SendTask> findByUser(User user);
    List<SendTask> findByUserId(Long userId);
    List<SendTask> findByStatus(SendTask.TaskStatus status);
    
    @Query("SELECT t FROM SendTask t WHERE t.status = 'ACTIVE' AND t.nextCheckTime <= :now")
    List<SendTask> findActiveTasksDueBy(LocalDateTime now);
    
    @Query("SELECT t FROM SendTask t WHERE t.status = 'ACTIVE' AND t.user.id = :userId")
    List<SendTask> findActiveTasksByUserId(Long userId);
}
