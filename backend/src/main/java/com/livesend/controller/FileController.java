package com.livesend.controller;

import com.livesend.dto.ApiResponse;
import com.livesend.entity.FileItem;
import com.livesend.service.FileService;
import com.livesend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "*")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ApiResponse<List<FileItem>> getAllFiles() {
        Long userId = userService.getDefaultUser().getId();
        List<FileItem> files = fileService.getFilesByUser(userId);
        return ApiResponse.success(files);
    }

    @GetMapping("/{id}")
    public ApiResponse<FileItem> getFile(@PathVariable Long id) {
        return fileService.getFileById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("文件不存在"));
    }

    @PostMapping("/upload")
    public ApiResponse<FileItem> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            Long userId = userService.getDefaultUser().getId();
            FileItem savedFile = fileService.uploadFile(file, userId);
            return ApiResponse.success("文件上传成功", savedFile);
        } catch (IOException e) {
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteFile(@PathVariable Long id) {
        try {
            fileService.deleteFile(id);
            return ApiResponse.success("文件删除成功", null);
        } catch (Exception e) {
            return ApiResponse.error("文件删除失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/preview")
    public ResponseEntity<byte[]> previewFile(@PathVariable Long id) {
        try {
            FileItem fileItem = fileService.getFileById(id)
                    .orElseThrow(() -> new RuntimeException("文件不存在"));
            
            byte[] content = fileService.getFileContent(id);
            
            String contentType = fileItem.getFileType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + 
                            URLEncoder.encode(fileItem.getOriginalName(), StandardCharsets.UTF_8) + "\"")
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long id) {
        try {
            FileItem fileItem = fileService.getFileById(id)
                    .orElseThrow(() -> new RuntimeException("文件不存在"));
            
            byte[] content = fileService.getFileContent(id);
            
            String contentType = fileItem.getFileType();
            if (contentType == null || contentType.isEmpty()) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + 
                            URLEncoder.encode(fileItem.getOriginalName(), StandardCharsets.UTF_8) + "\"")
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
