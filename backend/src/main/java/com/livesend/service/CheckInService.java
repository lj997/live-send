package com.livesend.service;

import com.livesend.entity.CheckInRecord;
import com.livesend.entity.SendTask;
import com.livesend.entity.User;
import com.livesend.repository.CheckInRecordRepository;
import com.livesend.repository.SendTaskRepository;
import com.livesend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CheckInService {

    @Autowired
    private CheckInRecordRepository checkInRecordRepository;

    @Autowired
    private SendTaskRepository sendTaskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SendTaskService sendTaskService;

    public List<CheckInRecord> getCheckInRecordsByTask(Long taskId) {
        return checkInRecordRepository.findByTaskId(taskId);
    }

    public List<CheckInRecord> getCheckInRecordsByUser(Long userId) {
        return checkInRecordRepository.findByUser(userRepository.getReferenceById(userId));
    }

    @Transactional
    public CheckInRecord checkIn(Long taskId, Long userId, String note) {
        SendTask task = sendTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        if (task.getStatus() != SendTask.TaskStatus.ACTIVE) {
            throw new RuntimeException("任务未激活，无法签到");
        }

        CheckInRecord record = new CheckInRecord();
        record.setTask(task);
        record.setUser(user);
        record.setCheckInTime(LocalDateTime.now());
        record.setNote(note);

        task.setCurrentCheckIns(task.getCurrentCheckIns() + 1);

        if (task.getCurrentCheckIns() >= task.getRequiredCheckIns()) {
            sendTaskService.resetTask(task);
        }

        sendTaskRepository.save(task);
        return checkInRecordRepository.save(record);
    }

    public boolean hasUserCheckedInToday(Long taskId, Long userId) {
        LocalDateTime today = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime tomorrow = today.plusDays(1);
        
        long count = checkInRecordRepository.countByTaskIdAndCheckInTimeBetween(
                taskId, today, tomorrow);
        return count > 0;
    }
}
