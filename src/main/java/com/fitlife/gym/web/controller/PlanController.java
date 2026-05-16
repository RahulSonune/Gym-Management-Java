package com.fitlife.gym.web.controller;

import com.fitlife.gym.service.PlanService;
import com.fitlife.gym.web.dto.MembershipPlanDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plans")
@RequiredArgsConstructor
public class PlanController {

    private final PlanService planService;

    @GetMapping
    public List<MembershipPlanDto> list(@RequestParam(name = "active", defaultValue = "true") boolean active) {
        return planService.list(active);
    }
}
