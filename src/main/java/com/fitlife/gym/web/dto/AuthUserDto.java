package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class AuthUserDto {
    Long id;
    String fullName;
    String email;
    List<String> roles;
    List<BranchSummaryDto> branches;
    OrganizationSummaryDto organization;
}
