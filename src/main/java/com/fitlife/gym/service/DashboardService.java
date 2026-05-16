package com.fitlife.gym.service;

import com.fitlife.gym.domain.repository.*;
import com.fitlife.gym.web.dto.DashboardSummaryDto;
import com.fitlife.gym.web.dto.ExpiringMemberDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final MemberRepository memberRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final BranchContextHelper branchContext;
    private final DtoMapper dtoMapper;

    @Transactional(readOnly = true)
    public DashboardSummaryDto getSummary(Long branchId) {
        Long effectiveBranchId = branchContext.resolveBranchId(branchId);
        ZoneId zone = ZoneId.of("Asia/Kolkata");
        LocalDate today = LocalDate.now(zone);
        Instant[] range = dayRange(today, zone);

        long activeMembers = memberRepository.countByBranchIdAndStatusAndDeletedAtIsNull(
                effectiveBranchId, "ACTIVE");
        long checkInsToday = attendanceLogRepository.countByBranchIdAndCheckInAtBetweenAndDeniedReasonIsNull(
                effectiveBranchId, range[0], range[1]);
        long currentlyInside = attendanceLogRepository
                .findCurrentlyInside(effectiveBranchId, range[0], range[1])
                .size();
        long expiringIn7Days = subscriptionRepository.countExpiringInBranch(
                effectiveBranchId, today, today.plusDays(7));
        long overdueInvoices = invoiceRepository.countByBranchIdAndStatus(effectiveBranchId, "OVERDUE");
        long revenueTodayMinor = paymentRepository.sumRevenueBetween(
                effectiveBranchId, range[0], range[1]);
        long newMembersToday = memberRepository.countNewMembersOnDate(effectiveBranchId, today);

        return DashboardSummaryDto.builder()
                .branchId(effectiveBranchId)
                .date(today.toString())
                .activeMembers(activeMembers)
                .checkInsToday(checkInsToday)
                .currentlyInside(currentlyInside)
                .expiringIn7Days(expiringIn7Days)
                .overdueInvoices(overdueInvoices)
                .revenueTodayMinor(revenueTodayMinor)
                .newMembersToday(newMembersToday)
                .build();
    }

    @Transactional(readOnly = true)
    public List<ExpiringMemberDto> getExpiring(Long branchId, int days) {
        Long effectiveBranchId = branchContext.resolveBranchId(branchId);
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(days);
        return subscriptionRepository.findExpiringInBranch(effectiveBranchId, today, end).stream()
                .map(dtoMapper::toExpiringMember)
                .toList();
    }

    private Instant[] dayRange(LocalDate date, ZoneId zone) {
        Instant start = date.atStartOfDay(zone).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(zone).toInstant();
        return new Instant[] {start, end};
    }
}
