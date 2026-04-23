package com.livesend.repository;

import com.livesend.entity.FileItem;
import com.livesend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileItemRepository extends JpaRepository<FileItem, Long> {
    List<FileItem> findByUser(User user);
    List<FileItem> findByUserId(Long userId);
}
