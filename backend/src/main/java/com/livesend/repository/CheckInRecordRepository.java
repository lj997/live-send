package com.livesend.repository;

import com.livesend.entity.CheckInRecord;
import com.livesend.entity.SendTask;
import com.livesend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CheckInRecordRepository extends JpaRepository<CheckInRecord, Long> {
    List<CheckInRecord> findByTask(SendTask task);
    List<CheckInRecord> findByUser(User user);
    List<CheckInRecord> findByTaskId(Long taskId);
    List<CheckInRecord> findByTaskIdAndCheckInTimeBetween(Long taskId, LocalDateTime start, LocalDateTime end);
    long countByTaskIdAndCheckInTimeBetween(Long taskId, LocalDateTime start, LocalDateTime end);
}
