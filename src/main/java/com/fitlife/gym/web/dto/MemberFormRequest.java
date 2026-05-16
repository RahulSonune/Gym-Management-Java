package com.fitlife.gym.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberFormRequest {

    @NotBlank
    private String firstName;

    private String lastName;

    @NotBlank
    private String phone;

    private String email;
    private String gender;
    private String dateOfBirth;
    private String emergencyContactName;
    private String emergencyContactPhone;
    private String source;
    private Long branchId;
}
