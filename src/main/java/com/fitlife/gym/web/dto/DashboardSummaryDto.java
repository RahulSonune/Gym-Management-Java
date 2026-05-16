package com.fitlife.gym.web.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DashboardSummaryDto {
    Long branchId;
    String date;
    long activeMembers;
    long checkInsToday;
    long currentlyInside;
    long expiringIn7Days;
    long overdueInvoices;
    long revenueTodayMinor;
    long newMembersToday;
}
