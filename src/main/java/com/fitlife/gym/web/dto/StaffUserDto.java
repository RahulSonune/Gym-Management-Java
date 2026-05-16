package com.fitlife.gym.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class StaffUserDto {
    Long id;
    String fullName;
    String email;
    String phone;
    List<String> roles;
    @JsonProperty("isActive")
    boolean isActive;
    List<String> branchNames;
    List<Long> branchIds;
    Long primaryBranchId;
}
