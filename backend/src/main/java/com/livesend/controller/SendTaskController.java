package com.livesend.controller;

import com.livesend.dto.ApiResponse;
import com.livesend.entity.SendTask;
import com.livesend.service.SendTaskService;
import com.livesend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@CrossOrigin(origins = "*")
public class SendTaskController {

    @Autowired
    private SendTaskService sendTaskService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ApiResponse<List<SendTask>> getAllTasks() {
        Long userId = userService.getDefaultUser().getId();
        List<SendTask> tasks = sendTaskService.getTasksByUser(userId);
        return ApiResponse.success(tasks);
    }

    @GetMapping("/active")
    public ApiResponse<List<SendTask>> getActiveTasks() {
        Long userId = userService.getDefaultUser().getId();
        List<SendTask> tasks = sendTaskService.getActiveTasksByUser(userId);
        return ApiResponse.success(tasks);
    }

    @GetMapping("/{id}")
    public ApiResponse<SendTask> getTask(@PathVariable Long id) {
        return sendTaskService.getTaskById(id)
                .map(ApiResponse::success)
                .orElse(ApiResponse.error("任务不存在"));
    }

    @PostMapping
    public ApiResponse<SendTask> createTask(@RequestBody Map<String, Object> request) {
        try {
            Long userId = userService.getDefaultUser().getId();
            String name = (String) request.get("name");
            
            @SuppressWarnings("unchecked")
            List<Long> fileIds = (List<Long>) request.get("fileIds");
            
            @SuppressWarnings("unchecked")
            List<Long> noteIds = (List<Long>) request.get("noteIds");
            
            @SuppressWarnings("unchecked")
            List<Long> contactIds = (List<Long>) request.get("contactIds");
            
            Integer countdownDays = ((Number) request.get("countdownDays")).intValue();
            Integer requiredCheckIns = ((Number) request.get("requiredCheckIns")).intValue();

            SendTask task = sendTaskService.createTask(
                    userId, name, fileIds, noteIds, contactIds, countdownDays, requiredCheckIns
            );
            return ApiResponse.success("任务创建成功", task);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("任务创建失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ApiResponse<SendTask> updateTask(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            
            @SuppressWarnings("unchecked")
            List<Long> fileIds = (List<Long>) request.get("fileIds");
            
            @SuppressWarnings("unchecked")
            List<Long> noteIds = (List<Long>) request.get("noteIds");
            
            @SuppressWarnings("unchecked")
            List<Long> contactIds = (List<Long>) request.get("contactIds");
            
            Integer countdownDays = request.containsKey("countdownDays") 
                    ? ((Number) request.get("countdownDays")).intValue() : null;
            Integer requiredCheckIns = request.containsKey("requiredCheckIns") 
                    ? ((Number) request.get("requiredCheckIns")).intValue() : null;

            SendTask task = sendTaskService.updateTask(
                    id, name, fileIds, noteIds, contactIds, countdownDays, requiredCheckIns
            );
            return ApiResponse.success("任务更新成功", task);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            return ApiResponse.error("任务更新失败: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/pause")
    public ApiResponse<Void> pauseTask(@PathVariable Long id) {
        try {
            sendTaskService.pauseTask(id);
            return ApiResponse.success("任务已暂停", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/resume")
    public ApiResponse<Void> resumeTask(@PathVariable Long id) {
        try {
            sendTaskService.resumeTask(id);
            return ApiResponse.success("任务已恢复", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<Void> cancelTask(@PathVariable Long id) {
        try {
            sendTaskService.cancelTask(id);
            return ApiResponse.success("任务已取消", null);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/send-now")
    public ApiResponse<Void> sendNow(@PathVariable Long id) {
        try {
            sendTaskService.validateAndGetTaskForSend(id);
            sendTaskService.sendTaskAsync(id);
            return ApiResponse.success("发送请求已提交，正在后台发送中...", null);
        } catch (Exception e) {
            return ApiResponse.error("发送失败: " + e.getMessage());
        }
    }
}
