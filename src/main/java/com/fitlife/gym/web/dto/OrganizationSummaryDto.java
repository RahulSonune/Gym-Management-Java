package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OrganizationSummaryDto {
    Long id;
    String name;
    boolean multiBranch;
    String currencyCode;
    String timezone;
    String logoUrl;
}
