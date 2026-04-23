package com.livesend.controller;

import com.livesend.dto.ApiResponse;
import com.livesend.entity.CheckInRecord;
import com.livesend.service.CheckInService;
import com.livesend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/checkin")
@CrossOrigin(origins = "*")
public class CheckInController {

    @Autowired
    private CheckInService checkInService;

    @Autowired
    private UserService userService;

    @GetMapping("/task/{taskId}")
    public ApiResponse<List<CheckInRecord>> getCheckInRecordsByTask(@PathVariable Long taskId) {
        List<CheckInRecord> records = checkInService.getCheckInRecordsByTask(taskId);
        return ApiResponse.success(records);
    }

    @GetMapping("/task/{taskId}/today")
    public ApiResponse<Boolean> hasCheckedInToday(@PathVariable Long taskId) {
        Long userId = userService.getDefaultUser().getId();
        boolean hasCheckedIn = checkInService.hasUserCheckedInToday(taskId, userId);
        return ApiResponse.success(hasCheckedIn);
    }

    @PostMapping("/task/{taskId}")
    public ApiResponse<CheckInRecord> checkIn(
            @PathVariable Long taskId,
            @RequestBody(required = false) Map<String, String> request) {
        try {
            Long userId = userService.getDefaultUser().getId();
            String note = request != null ? request.get("note") : null;
            CheckInRecord record = checkInService.checkIn(taskId, userId, note);
            return ApiResponse.success("签到成功", record);
        } catch (Exception e) {
            return ApiResponse.error(e.getMessage());
        }
    }
}
