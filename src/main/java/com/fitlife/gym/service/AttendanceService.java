package com.fitlife.gym.service;

import com.fitlife.gym.domain.entity.*;
import com.fitlife.gym.domain.repository.AttendanceLogRepository;
import com.fitlife.gym.domain.repository.MemberRepository;
import com.fitlife.gym.domain.repository.SubscriptionRepository;
import com.fitlife.gym.security.GymUserPrincipal;
import com.fitlife.gym.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceLogRepository attendanceLogRepository;
    private final MemberRepository memberRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final BranchContextHelper branchContext;
    private final DtoMapper dtoMapper;

    @Transactional(readOnly = true)
    public List<AttendanceLogDto> list(Long branchId) {
        Long effectiveBranchId = branchContext.resolveBranchId(branchId);
        Instant[] range = todayRange();
        return attendanceLogRepository
                .findByBranchAndDateRange(effectiveBranchId, range[0], range[1])
                .stream()
                .map(dtoMapper::toAttendanceDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LiveAttendanceDto> live(Long branchId) {
        Long effectiveBranchId = branchContext.resolveBranchId(branchId);
        Instant[] range = todayRange();
        return attendanceLogRepository.findCurrentlyInside(effectiveBranchId, range[0], range[1]).stream()
                .map(log -> {
                    String planName = subscriptionRepository
                            .findActiveForMember(log.getMember().getId(), LocalDate.now())
                            .map(sub -> sub.getPlan().getName())
                            .orElse(null);
                    return dtoMapper.toLiveAttendance(log, planName);
                })
                .toList();
    }

    @Transactional
    public CheckInResponse checkIn(CheckInRequest request) {
        GymUserPrincipal user = branchContext.currentUser();
        branchContext.assertBranchAccess(user, request.getBranchId());

        Member member = memberRepository
                .findByIdAndOrganizationId(request.getMemberId(), user.getOrganizationId())
                .orElse(null);

        if (member == null) {
            return denied("MEMBER_NOT_FOUND", "Member not found", request, null);
        }

        Optional<Subscription> activeSub = subscriptionRepository.findActiveForMember(
                member.getId(), LocalDate.now());

        if (activeSub.isEmpty()) {
            return denied("SUBSCRIPTION_EXPIRED", "Membership expired or inactive", request, member);
        }

        Subscription subscription = activeSub.get();
        Instant[] range = todayRange();
        Optional<AttendanceLog> open = attendanceLogRepository.findOpenCheckIn(
                member.getId(), request.getBranchId(), range[0]);
        if (open.isPresent()) {
            AttendanceLog existing = open.get();
            return CheckInResponse.builder()
                    .allowed(true)
                    .attendanceId(existing.getId())
                    .checkInAt(existing.getCheckInAt())
                    .member(CheckInResponse.MemberBriefDto.builder()
                            .id(member.getId())
                            .fullName(member.getFullName())
                            .memberCode(member.getMemberCode())
                            .build())
                    .subscription(CheckInResponse.SubscriptionBriefDto.builder()
                            .planName(subscription.getPlan().getName())
                            .endDate(subscription.getEndDate().toString())
                            .build())
                    .message("Already checked in")
                    .build();
        }

        Instant now = Instant.now();
        AttendanceLog log = AttendanceLog.builder()
                .organization(member.getOrganization())
                .branch(member.getBranch())
                .member(member)
                .checkInAt(now)
                .method(request.getMethod())
                .deviceId(request.getDeviceId())
                .subscriptionId(subscription.getId())
                .createdByUserId(user.getUserId())
                .build();
        log = attendanceLogRepository.save(log);

        return CheckInResponse.builder()
                .allowed(true)
                .attendanceId(log.getId())
                .checkInAt(now)
                .member(CheckInResponse.MemberBriefDto.builder()
                        .id(member.getId())
                        .fullName(member.getFullName())
                        .memberCode(member.getMemberCode())
                        .build())
                .subscription(CheckInResponse.SubscriptionBriefDto.builder()
                        .planName(subscription.getPlan().getName())
                        .endDate(subscription.getEndDate().toString())
                        .build())
                .build();
    }

    @Transactional
    public CheckOutResponse checkOut(CheckOutRequest request) {
        GymUserPrincipal user = branchContext.currentUser();
        branchContext.assertBranchAccess(user, request.getBranchId());

        Member member = memberRepository
                .findByIdAndOrganizationId(request.getMemberId(), user.getOrganizationId())
                .orElse(null);

        if (member == null) {
            return CheckOutResponse.builder()
                    .allowed(false)
                    .deniedReason("MEMBER_NOT_FOUND")
                    .message("Member not found")
                    .build();
        }

        Instant[] range = todayRange();
        Optional<AttendanceLog> open = attendanceLogRepository.findOpenCheckIn(
                member.getId(), request.getBranchId(), range[0]);

        if (open.isEmpty()) {
            return CheckOutResponse.builder()
                    .allowed(false)
                    .deniedReason("NOT_CHECKED_IN")
                    .message("Member is not checked in")
                    .build();
        }

        AttendanceLog log = open.get();
        Instant now = Instant.now();
        log.setCheckOutAt(now);
        attendanceLogRepository.save(log);

        return CheckOutResponse.builder()
                .allowed(true)
                .attendanceId(log.getId())
                .checkInAt(log.getCheckInAt())
                .checkOutAt(now)
                .member(CheckInResponse.MemberBriefDto.builder()
                        .id(member.getId())
                        .fullName(member.getFullName())
                        .memberCode(member.getMemberCode())
                        .build())
                .message("Checked out successfully")
                .build();
    }

    private CheckInResponse denied(
            String reason, String message, CheckInRequest request, Member member) {
        if (member != null) {
            AttendanceLog deniedLog = AttendanceLog.builder()
                    .organization(member.getOrganization())
                    .branch(member.getBranch())
                    .member(member)
                    .checkInAt(Instant.now())
                    .method(request.getMethod())
                    .deviceId(request.getDeviceId())
                    .deniedReason(reason)
                    .build();
            attendanceLogRepository.save(deniedLog);
        }
        return CheckInResponse.builder()
                .allowed(false)
                .deniedReason(reason)
                .message(message)
                .build();
    }

    private Instant[] todayRange() {
        ZoneId zone = ZoneId.of("Asia/Kolkata");
        LocalDate today = LocalDate.now(zone);
        Instant start = today.atStartOfDay(zone).toInstant();
        Instant end = today.plusDays(1).atStartOfDay(zone).toInstant();
        return new Instant[] {start, end};
    }
}
