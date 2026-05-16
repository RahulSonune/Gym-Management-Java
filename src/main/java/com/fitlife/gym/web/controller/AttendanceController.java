package com.fitlife.gym.web.controller;

import com.fitlife.gym.service.AttendanceService;
import com.fitlife.gym.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping
    public List<AttendanceLogDto> list(@RequestParam Long branchId) {
        return attendanceService.list(branchId);
    }

    @GetMapping("/live")
    public List<LiveAttendanceDto> live(@RequestParam Long branchId) {
        return attendanceService.live(branchId);
    }

    @PostMapping({"/check-in", "/check-in/", "/checkin"})
    public CheckInResponse checkIn(@Valid @RequestBody CheckInRequest request) {
        return attendanceService.checkIn(request);
    }

    @PostMapping({"/check-out", "/check-out/", "/checkout"})
    public CheckOutResponse checkOut(@Valid @RequestBody CheckOutRequest request) {
        return attendanceService.checkOut(request);
    }
}
