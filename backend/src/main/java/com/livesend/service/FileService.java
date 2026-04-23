package com.livesend.service;

import com.livesend.entity.FileItem;
import com.livesend.entity.User;
import com.livesend.repository.FileItemRepository;
import com.livesend.repository.UserRepository;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileService {

    @Autowired
    private FileItemRepository fileItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${file.upload.path}")
    private String uploadPath;

    public List<FileItem> getFilesByUser(Long userId) {
        return fileItemRepository.findByUserId(userId);
    }

    public Optional<FileItem> getFileById(Long id) {
        return fileItemRepository.findById(id);
    }

    @Transactional
    public FileItem uploadFile(MultipartFile file, Long userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));

        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String originalName = file.getOriginalFilename();
        String extension = FilenameUtils.getExtension(originalName);
        String fileName = UUID.randomUUID().toString() + "." + extension;
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        
        Path dateDir = uploadDir.resolve(timestamp);
        if (!Files.exists(dateDir)) {
            Files.createDirectories(dateDir);
        }

        Path filePath = dateDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        FileItem fileItem = new FileItem();
        fileItem.setFileName(fileName);
        fileItem.setOriginalName(originalName);
        fileItem.setFilePath(filePath.toString());
        fileItem.setFileType(file.getContentType());
        fileItem.setFileSize(file.getSize());
        fileItem.setUser(user);

        return fileItemRepository.save(fileItem);
    }

    @Transactional
    public void deleteFile(Long id) {
        FileItem fileItem = fileItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        
        try {
            Path filePath = Paths.get(fileItem.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        fileItemRepository.deleteById(id);
    }

    public byte[] getFileContent(Long id) throws IOException {
        FileItem fileItem = fileItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文件不存在"));
        Path filePath = Paths.get(fileItem.getFilePath());
        return Files.readAllBytes(filePath);
    }
}
