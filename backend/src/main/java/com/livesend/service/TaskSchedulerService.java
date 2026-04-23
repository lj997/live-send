package com.livesend.service;

import com.livesend.entity.SendTask;
import com.livesend.repository.SendTaskRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskSchedulerService {

    @Autowired
    private SendTaskRepository sendTaskRepository;

    @Autowired
    private SendTaskService sendTaskService;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void checkTasks() {
        LocalDateTime now = LocalDateTime.now();
        List<SendTask> dueTasks = sendTaskRepository.findActiveTasksDueBy(now);

        for (SendTask task : dueTasks) {
            processTask(task);
        }
    }

    @Transactional
    public void processTask(SendTask task) {
        if (task.getCurrentCheckIns() < task.getRequiredCheckIns()) {
            try {
                emailService.sendEmailWithAttachments(task);
                sendTaskService.markTaskCompleted(task);
            } catch (MessagingException e) {
                e.printStackTrace();
            }
        } else {
            sendTaskService.resetTask(task);
        }
    }
}
