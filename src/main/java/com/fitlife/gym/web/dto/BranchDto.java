package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BranchDto {
    Long id;
    Long organizationId;
    String code;
    String name;
    String addressLine1;
    String city;
    String state;
    String postalCode;
    String country;
    String phone;
    String email;
    String timezone;
    boolean isDefault;
    boolean isActive;
}
