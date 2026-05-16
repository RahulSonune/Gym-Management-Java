package com.fitlife.gym.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class CreateStaffRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 512)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Size(max = 512)
    private String email;

    @Size(max = 512)
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(max = 512)
    private String password;

    @NotEmpty(message = "Select at least one role")
    private List<String> roles;

    @NotEmpty(message = "Select at least one branch")
    private List<Long> branchIds;

    private Long primaryBranchId;
}
