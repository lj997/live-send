package com.livesend.service;

import com.livesend.entity.*;
import com.livesend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SendTaskService {

    @Autowired
    private SendTaskRepository sendTaskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FileItemRepository fileItemRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private CheckInRecordRepository checkInRecordRepository;

    @Autowired
    private EmailService emailService;

    public List<SendTask> getTasksByUser(Long userId) {
        return sendTaskRepository.findByUserId(userId);
    }

    public List<SendTask> getActiveTasksByUser(Long userId) {
        return sendTaskRepository.findActiveTasksByUserId(userId);
    }

    public Optional<SendTask> getTaskById(Long id) {
        return sendTaskRepository.findById(id);
    }

    @Transactional
    public SendTask createTask(Long userId, String name, List<Long> fileIds, 
                                List<Long> noteIds, List<Long> contactIds, 
                                Integer countdownDays, Integer requiredCheckIns) {
        if (requiredCheckIns > countdownDays) {
            throw new IllegalArgumentException("签到次数不能大于倒计时天数");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        List<FileItem> files = fileIds != null ? fileItemRepository.findAllById(fileIds) : new ArrayList<>();
        List<Note> notes = noteIds != null ? noteRepository.findAllById(noteIds) : new ArrayList<>();
        List<Contact> contacts = contactRepository.findAllById(contactIds);

        if (files.isEmpty() && notes.isEmpty()) {
            throw new RuntimeException("请至少选择一个文件或一篇笔记");
        }
        if (contacts.isEmpty()) {
            throw new RuntimeException("请至少选择一个联系人");
        }

        SendTask task = new SendTask();
        task.setName(name);
        task.setFiles(files);
        task.setNotes(notes);
        task.setContacts(contacts);
        task.setCountdownDays(countdownDays);
        task.setRequiredCheckIns(requiredCheckIns);
        task.setCurrentCheckIns(0);
        task.setLastResetTime(LocalDateTime.now());
        task.setNextCheckTime(LocalDateTime.now().plusDays(countdownDays));
        task.setStatus(SendTask.TaskStatus.ACTIVE);
        task.setUser(user);

        return sendTaskRepository.save(task);
    }

    @Transactional
    public SendTask updateTask(Long taskId, String name, List<Long> fileIds,
                                List<Long> noteIds, List<Long> contactIds, 
                                Integer countdownDays, Integer requiredCheckIns) {
        SendTask task = sendTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        if (requiredCheckIns != null && countdownDays != null 
                && requiredCheckIns > countdownDays) {
            throw new IllegalArgumentException("签到次数不能大于倒计时天数");
        }

        if (name != null) {
            task.setName(name);
        }
        if (fileIds != null) {
            List<FileItem> files = fileItemRepository.findAllById(fileIds);
            if (!files.isEmpty() || (task.getNotes() != null && !task.getNotes().isEmpty())) {
                task.setFiles(files);
            }
        }
        if (noteIds != null) {
            List<Note> notes = noteRepository.findAllById(noteIds);
            task.setNotes(notes);
        }
        if (contactIds != null) {
            List<Contact> contacts = contactRepository.findAllById(contactIds);
            if (!contacts.isEmpty()) {
                task.setContacts(contacts);
            }
        }
        if (countdownDays != null) {
            task.setCountdownDays(countdownDays);
        }
        if (requiredCheckIns != null) {
            task.setRequiredCheckIns(requiredCheckIns);
        }

        return sendTaskRepository.save(task);
    }

    @Transactional
    public void pauseTask(Long taskId) {
        SendTask task = sendTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        task.setStatus(SendTask.TaskStatus.PAUSED);
        sendTaskRepository.save(task);
    }

    @Transactional
    public void resumeTask(Long taskId) {
        SendTask task = sendTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        task.setStatus(SendTask.TaskStatus.ACTIVE);
        sendTaskRepository.save(task);
    }

    @Transactional
    public void cancelTask(Long taskId) {
        SendTask task = sendTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        task.setStatus(SendTask.TaskStatus.CANCELLED);
        sendTaskRepository.save(task);
    }

    @Transactional
    public void resetTask(SendTask task) {
        task.setCurrentCheckIns(0);
        task.setLastResetTime(LocalDateTime.now());
        task.setNextCheckTime(LocalDateTime.now().plusDays(task.getCountdownDays()));
        sendTaskRepository.save(task);
    }

    @Transactional
    public void markTaskCompleted(SendTask task) {
        task.setStatus(SendTask.TaskStatus.COMPLETED);
        sendTaskRepository.save(task);
    }

    public SendTask validateAndGetTaskForSend(Long taskId) {
        SendTask task = sendTaskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        if (task.getStatus() == SendTask.TaskStatus.COMPLETED) {
            throw new RuntimeException("该任务已完成，邮件已发送");
        }
        if (task.getStatus() == SendTask.TaskStatus.CANCELLED) {
            throw new RuntimeException("该任务已取消，无法发送");
        }
        
        return task;
    }

    @Async
    @Transactional
    public void sendTaskAsync(Long taskId) {
        try {
            SendTask task = sendTaskRepository.findById(taskId).orElse(null);
            if (task == null) {
                return;
            }
            
            emailService.sendEmailWithAttachments(task);
            task.setStatus(SendTask.TaskStatus.COMPLETED);
            sendTaskRepository.save(task);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
