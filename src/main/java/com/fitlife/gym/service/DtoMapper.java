package com.fitlife.gym.service;

import com.fitlife.gym.domain.entity.*;
import com.fitlife.gym.web.dto.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DtoMapper {

    public OrganizationSummaryDto toOrganizationSummary(Organization org) {
        return OrganizationSummaryDto.builder()
                .id(org.getId())
                .name(org.getName())
                .multiBranch(org.isMultiBranch())
                .currencyCode(org.getCurrencyCode())
                .timezone(org.getTimezone())
                .logoUrl(org.getLogoUrl())
                .build();
    }

    public BranchSummaryDto toBranchSummary(Branch branch, boolean isPrimary) {
        return BranchSummaryDto.builder()
                .id(branch.getId())
                .name(branch.getName())
                .code(branch.getCode())
                .isPrimary(isPrimary)
                .isActive(branch.isActive())
                .build();
    }

    public BranchDto toBranchDto(Branch branch) {
        return BranchDto.builder()
                .id(branch.getId())
                .organizationId(branch.getOrganization().getId())
                .code(branch.getCode())
                .name(branch.getName())
                .addressLine1(branch.getAddressLine1())
                .city(branch.getCity())
                .state(branch.getState())
                .postalCode(branch.getPostalCode())
                .country(branch.getCountry())
                .phone(branch.getPhone())
                .email(branch.getEmail())
                .timezone(branch.getTimezone())
                .isDefault(branch.isDefault())
                .isActive(branch.isActive())
                .build();
    }

    public MemberDto toMemberDto(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .memberCode(member.getMemberCode())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .fullName(member.getFullName())
                .phone(member.getPhone())
                .email(member.getEmail())
                .gender(member.getGender())
                .dateOfBirth(member.getDateOfBirth())
                .status(member.getStatus())
                .branchId(member.getBranch().getId())
                .branch(toBranchSummary(member.getBranch(), false))
                .emergencyContactName(member.getEmergencyContactName())
                .emergencyContactPhone(member.getEmergencyContactPhone())
                .joinedAt(formatDate(member.getJoinedAt()))
                .source(member.getSource())
                .createdAt(member.getCreatedAt())
                .build();
    }

    public MemberProfileDto toMemberProfile(
            Member member, SubscriptionSummaryDto subscription, MemberStatsDto stats) {
        return MemberProfileDto.builder()
                .id(member.getId())
                .memberCode(member.getMemberCode())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .fullName(member.getFullName())
                .phone(member.getPhone())
                .email(member.getEmail())
                .gender(member.getGender())
                .dateOfBirth(member.getDateOfBirth())
                .status(member.getStatus())
                .branchId(member.getBranch().getId())
                .branch(toBranchSummary(member.getBranch(), false))
                .emergencyContactName(member.getEmergencyContactName())
                .emergencyContactPhone(member.getEmergencyContactPhone())
                .joinedAt(formatDate(member.getJoinedAt()))
                .source(member.getSource())
                .createdAt(member.getCreatedAt())
                .activeSubscription(subscription)
                .stats(stats)
                .build();
    }

    public SubscriptionSummaryDto toSubscriptionSummary(Subscription subscription) {
        LocalDate today = LocalDate.now();
        long daysRemaining = ChronoUnit.DAYS.between(today, subscription.getEndDate());
        return SubscriptionSummaryDto.builder()
                .id(subscription.getId())
                .planName(subscription.getPlan().getName())
                .status(subscription.getStatus())
                .startDate(formatDate(subscription.getStartDate()))
                .endDate(formatDate(subscription.getEndDate()))
                .daysRemaining(Math.max(daysRemaining, 0))
                .build();
    }

    public MembershipPlanDto toPlanDto(MembershipPlan plan) {
        Long branchId = plan.getBranch() != null ? plan.getBranch().getId() : null;
        return MembershipPlanDto.builder()
                .id(plan.getId())
                .code(plan.getCode())
                .name(plan.getName())
                .description(plan.getDescription())
                .durationDays(plan.getDurationDays())
                .priceAmountMinor(plan.getPriceAmountMinor())
                .currencyCode(plan.getCurrencyCode())
                .taxPercent(plan.getTaxPercent())
                .maxFreezeDays(plan.getMaxFreezeDays())
                .allowsPt(plan.isAllowsPt())
                .allowsClasses(plan.isAllowsClasses())
                .branchId(branchId)
                .isActive(plan.isActive())
                .build();
    }

    public InvoiceDto toInvoiceDto(Invoice invoice) {
        return InvoiceDto.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .memberId(invoice.getMember().getId())
                .memberName(invoice.getMember().getFullName())
                .branchId(invoice.getBranch().getId())
                .status(invoice.getStatus())
                .issueDate(formatDate(invoice.getIssueDate()))
                .dueDate(formatDate(invoice.getDueDate()))
                .subtotalMinor(invoice.getSubtotalMinor())
                .taxMinor(invoice.getTaxMinor())
                .discountMinor(invoice.getDiscountMinor())
                .totalMinor(invoice.getTotalMinor())
                .amountPaidMinor(invoice.getAmountPaidMinor())
                .currencyCode(invoice.getCurrencyCode())
                .build();
    }

    public PaymentDto toPaymentDto(Payment payment) {
        return PaymentDto.builder()
                .id(payment.getId())
                .paymentNumber(payment.getPaymentNumber())
                .memberId(payment.getMember().getId())
                .memberName(payment.getMember().getFullName())
                .branchId(payment.getBranch().getId())
                .amountMinor(payment.getAmountMinor())
                .currencyCode(payment.getCurrencyCode())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .notes(payment.getNotes())
                .build();
    }

    public AttendanceLogDto toAttendanceDto(AttendanceLog log) {
        Member member = log.getMember();
        return AttendanceLogDto.builder()
                .id(log.getId())
                .memberId(member.getId())
                .memberName(member.getFullName())
                .memberCode(member.getMemberCode())
                .branchId(log.getBranch().getId())
                .checkInAt(log.getCheckInAt())
                .checkOutAt(log.getCheckOutAt())
                .method(log.getMethod())
                .build();
    }

    public LiveAttendanceDto toLiveAttendance(AttendanceLog log, String planName) {
        return LiveAttendanceDto.builder()
                .attendanceId(log.getId())
                .memberId(log.getMember().getId())
                .fullName(log.getMember().getFullName())
                .memberCode(log.getMember().getMemberCode())
                .checkInAt(log.getCheckInAt())
                .planName(planName)
                .build();
    }

    public ExpiringMemberDto toExpiringMember(Subscription subscription) {
        Member member = subscription.getMember();
        LocalDate today = LocalDate.now();
        long daysRemaining = ChronoUnit.DAYS.between(today, subscription.getEndDate());
        return ExpiringMemberDto.builder()
                .memberId(member.getId())
                .memberCode(member.getMemberCode())
                .fullName(member.getFullName())
                .phone(member.getPhone())
                .planName(subscription.getPlan().getName())
                .endDate(formatDate(subscription.getEndDate()))
                .daysRemaining(Math.max(daysRemaining, 0))
                .build();
    }

    public AuthUserDto toAuthUser(
            AppUser user,
            List<BranchSummaryDto> branches,
            OrganizationSummaryDto organization) {
        List<String> roles = user.getRoles().stream().sorted().collect(Collectors.toList());
        return AuthUserDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .roles(roles)
                .branches(branches)
                .organization(organization)
                .build();
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.toString() : null;
    }
}
