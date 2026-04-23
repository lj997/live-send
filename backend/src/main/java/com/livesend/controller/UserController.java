package com.livesend.controller;

import com.livesend.dto.ApiResponse;
import com.livesend.dto.UserDTO;
import com.livesend.entity.User;
import com.livesend.service.EmailService;
import com.livesend.service.UserService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/current")
    public ApiResponse<UserDTO> getCurrentUser() {
        User user = userService.getDefaultUser();
        return ApiResponse.success(UserDTO.fromEntity(user));
    }

    @PutMapping("/{id}/email-config")
    public ApiResponse<UserDTO> updateEmailConfig(
            @PathVariable Long id,
            @RequestBody User updatedUser) {
        try {
            User user = userService.updateEmailConfig(id, updatedUser);
            return ApiResponse.success("邮箱配置更新成功", UserDTO.fromEntity(user));
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/test-email")
    public ApiResponse<String> testEmail(@PathVariable Long id, @RequestParam String toEmail) {
        try {
            User user = userService.findById(id)
                    .orElseThrow(() -> new RuntimeException("用户不存在"));
            emailService.sendTestEmail(user, toEmail);
            return ApiResponse.success("测试邮件发送成功", null);
        } catch (MessagingException e) {
            return ApiResponse.error("邮件发送失败: " + e.getMessage());
        }
    }
}
