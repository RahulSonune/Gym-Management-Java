package com.fitlife.gym.web.controller;

import com.fitlife.gym.service.DashboardService;
import com.fitlife.gym.web.dto.DashboardSummaryDto;
import com.fitlife.gym.web.dto.ExpiringMemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/api/v1/dashboard/summary")
    public DashboardSummaryDto summary(@RequestParam Long branchId) {
        return dashboardService.getSummary(branchId);
    }

    @GetMapping("/api/v1/reports/expiring")
    public List<ExpiringMemberDto> expiring(
            @RequestParam Long branchId,
            @RequestParam(defaultValue = "30") int days) {
        return dashboardService.getExpiring(branchId, days);
    }
}
