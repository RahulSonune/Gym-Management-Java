package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class BranchSummaryDto {
    Long id;
    String name;
    String code;
    boolean isPrimary;
    Boolean isActive;
}
