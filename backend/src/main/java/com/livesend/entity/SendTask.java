package com.livesend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "send_tasks")
public class SendTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToMany
    @JoinTable(
        name = "task_files",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "file_id")
    )
    private List<FileItem> files = new ArrayList<>();

    @ManyToMany
    @JoinTable(
        name = "task_contacts",
        joinColumns = @JoinColumn(name = "task_id"),
        inverseJoinColumns = @JoinColumn(name = "contact_id")
    )
    private List<Contact> contacts = new ArrayList<>();

    @Column(nullable = false)
    private Integer countdownDays;

    @Column(nullable = false)
    private Integer requiredCheckIns;

    private Integer currentCheckIns = 0;

    private LocalDateTime lastResetTime;

    private LocalDateTime nextCheckTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.ACTIVE;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public enum TaskStatus {
        ACTIVE, PAUSED, COMPLETED, CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (lastResetTime == null) {
            lastResetTime = LocalDateTime.now();
        }
        if (nextCheckTime == null) {
            nextCheckTime = LocalDateTime.now().plusDays(countdownDays);
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
