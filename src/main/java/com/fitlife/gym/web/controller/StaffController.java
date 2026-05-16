package com.fitlife.gym.web.controller;

import com.fitlife.gym.service.StaffService;
import com.fitlife.gym.web.dto.CreateStaffRequest;
import com.fitlife.gym.web.dto.StaffUserDto;
import com.fitlife.gym.web.dto.UpdateStaffRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/staff")
@RequiredArgsConstructor
public class StaffController {

    private final StaffService staffService;

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public List<StaffUserDto> list() {
        return staffService.list();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public StaffUserDto getById(@PathVariable Long id) {
        return staffService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public StaffUserDto create(@Valid @RequestBody CreateStaffRequest request) {
        return staffService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'BRANCH_MANAGER')")
    public StaffUserDto update(@PathVariable Long id, @Valid @RequestBody UpdateStaffRequest request) {
        return staffService.update(id, request);
    }
}
