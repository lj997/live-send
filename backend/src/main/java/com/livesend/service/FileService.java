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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class FileService {

    private static final Set<String> BLOCKED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "php", "php3", "php4", "php5", "php7", "php8", "phtml",
            "jsp", "jspx", "asp", "aspx", "asmx", "ashx",
            "exe", "bat", "cmd", "sh", "bash", "cgi",
            "htaccess", "htpasswd", "ini", "conf",
            "sql", "sqlite", "db", "sqlite3",
            "jar", "war", "ear", "class",
            "dll", "so", "dylib"
    ));

    private static final Set<String> ALLOWED_EXTENSIONS = new HashSet<>(Arrays.asList(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "txt", "md", "rtf", "csv",
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg",
            "mp3", "wav", "flac", "aac", "ogg",
            "mp4", "avi", "mov", "mkv", "webm",
            "zip", "rar", "7z", "tar", "gz"
    ));

    @Autowired
    private FileItemRepository fileItemRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.allow-all:false}")
    private boolean allowAllExtensions;

    @Value("${file.upload.max-size:52428800}")
    private long maxFileSize;

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

        if (file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }

        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("文件大小超出限制，最大允许 " + formatFileSize(maxFileSize));
        }

        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isEmpty()) {
            throw new IllegalArgumentException("文件名不能为空");
        }

        String extension = FilenameUtils.getExtension(originalName).toLowerCase();
        if (extension.isEmpty()) {
            throw new IllegalArgumentException("文件必须有扩展名");
        }

        if (BLOCKED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不允许上传此类型的文件");
        }

        if (!allowAllExtensions && !ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件类型: " + extension);
        }

        Path uploadDir = Paths.get(uploadPath);
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

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

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        if (bytes < 1024 * 1024 * 1024) return (bytes / (1024 * 1024)) + " MB";
        return (bytes / (1024 * 1024 * 1024)) + " GB";
    }
}
