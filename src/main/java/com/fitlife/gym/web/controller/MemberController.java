package com.fitlife.gym.web.controller;

import com.fitlife.gym.service.MemberService;
import com.fitlife.gym.web.dto.MemberDto;
import com.fitlife.gym.web.dto.MemberFormRequest;
import com.fitlife.gym.web.dto.MemberProfileDto;
import com.fitlife.gym.web.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public PageResponse<MemberDto> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long branchId,
            @RequestParam(required = false) String status) {
        return memberService.list(page, size, search, branchId, status);
    }

    @GetMapping("/lookup")
    public List<MemberDto> lookup(@RequestParam String q) {
        return memberService.lookup(q);
    }

    @GetMapping("/{id}")
    public MemberProfileDto getById(@PathVariable Long id) {
        return memberService.getById(id);
    }

    @PostMapping
    public MemberDto create(@Valid @RequestBody MemberFormRequest request) {
        return memberService.create(request);
    }

    @PutMapping("/{id}")
    public MemberDto update(@PathVariable Long id, @Valid @RequestBody MemberFormRequest request) {
        return memberService.update(id, request);
    }
}
