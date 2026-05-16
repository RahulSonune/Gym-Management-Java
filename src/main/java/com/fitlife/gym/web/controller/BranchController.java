package com.fitlife.gym.web.controller;

import com.fitlife.gym.service.BranchService;
import com.fitlife.gym.web.dto.BranchDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/branches")
@RequiredArgsConstructor
public class BranchController {

    private final BranchService branchService;

    @GetMapping
    public List<BranchDto> list() {
        return branchService.listForCurrentUser();
    }
}
