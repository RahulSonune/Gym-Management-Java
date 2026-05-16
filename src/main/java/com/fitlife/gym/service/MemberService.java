package com.fitlife.gym.service;

import com.fitlife.gym.domain.entity.*;
import com.fitlife.gym.domain.repository.*;
import com.fitlife.gym.exception.BusinessRuleException;
import com.fitlife.gym.exception.ResourceNotFoundException;
import com.fitlife.gym.security.GymUserPrincipal;
import com.fitlife.gym.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final BranchRepository branchRepository;
    private final MemberSequenceRepository memberSequenceRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final BranchContextHelper branchContext;
    private final DtoMapper dtoMapper;

    @Transactional(readOnly = true)
    public PageResponse<MemberDto> list(
            int page, int size, String search, Long branchId, String status) {
        GymUserPrincipal user = branchContext.currentUser();
        Long effectiveBranchId = branchContext.resolveBranchIdOptional(branchId);
        Pageable pageable = PageRequest.of(page, size);
        Page<Member> result = memberRepository.search(
                user.getOrganizationId(), effectiveBranchId, blankToNull(status), blankToNull(search), pageable);
        return PageResponse.from(result.map(dtoMapper::toMemberDto));
    }

    @Transactional(readOnly = true)
    public MemberProfileDto getById(Long id) {
        GymUserPrincipal user = branchContext.currentUser();
        Member member = memberRepository.findByIdAndOrganizationId(id, user.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        branchContext.assertBranchAccess(user, member.getBranch().getId());
        return buildProfile(member);
    }

    @Transactional(readOnly = true)
    public List<MemberDto> lookup(String q) {
        GymUserPrincipal user = branchContext.currentUser();
        Long branchId = branchContext.resolveBranchId(null);
        if (q == null || q.isBlank()) {
            return List.of();
        }
        return memberRepository.lookup(user.getOrganizationId(), branchId, q.trim()).stream()
                .map(dtoMapper::toMemberDto)
                .toList();
    }

    @Transactional
    public MemberDto create(MemberFormRequest request) {
        GymUserPrincipal user = branchContext.currentUser();
        Long branchId = request.getBranchId() != null
                ? request.getBranchId()
                : branchContext.resolveBranchId(null);
        branchContext.assertBranchAccess(user, branchId);

        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        assertNoDuplicatePhone(branch.getOrganization().getId(), null, request.getPhone());

        String memberCode = nextMemberCode(branchId);
        Member member = Member.builder()
                .organization(branch.getOrganization())
                .branch(branch)
                .memberCode(memberCode)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .gender(request.getGender())
                .dateOfBirth(blankToNull(request.getDateOfBirth()))
                .emergencyContactName(request.getEmergencyContactName())
                .emergencyContactPhone(request.getEmergencyContactPhone())
                .status("PROSPECT")
                .joinedAt(LocalDate.now())
                .source(request.getSource())
                .createdByUserId(user.getUserId())
                .build();

        member = memberRepository.save(member);
        return dtoMapper.toMemberDto(member);
    }

    @Transactional
    public MemberDto update(Long id, MemberFormRequest request) {
        GymUserPrincipal user = branchContext.currentUser();
        Member member = memberRepository.findByIdAndOrganizationId(id, user.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));
        branchContext.assertBranchAccess(user, member.getBranch().getId());

        if (request.getFirstName() != null) {
            member.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            member.setLastName(request.getLastName());
        }
        if (request.getPhone() != null) {
            member.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            member.setEmail(request.getEmail());
        }
        if (request.getGender() != null) {
            member.setGender(request.getGender());
        }
        if (request.getDateOfBirth() != null) {
            member.setDateOfBirth(blankToNull(request.getDateOfBirth()));
        }
        if (request.getEmergencyContactName() != null) {
            member.setEmergencyContactName(request.getEmergencyContactName());
        }
        if (request.getEmergencyContactPhone() != null) {
            member.setEmergencyContactPhone(request.getEmergencyContactPhone());
        }
        if (request.getSource() != null) {
            member.setSource(request.getSource());
        }

        assertNoDuplicatePhone(
                member.getOrganization().getId(),
                member.getId(),
                member.getPhone());

        member = memberRepository.save(member);
        return dtoMapper.toMemberDto(member);
    }

    private MemberProfileDto buildProfile(Member member) {
        SubscriptionSummaryDto subscription = subscriptionRepository
                .findActiveForMember(member.getId(), LocalDate.now())
                .map(dtoMapper::toSubscriptionSummary)
                .orElse(null);

        long totalVisits = attendanceLogRepository.countByMemberIdAndDeniedReasonIsNull(member.getId());
        Instant lastVisit = attendanceLogRepository.findLastVisitAt(member.getId()).orElse(null);
        long outstanding = invoiceRepository.sumOutstandingForMember(member.getId());

        MemberStatsDto stats = MemberStatsDto.builder()
                .totalVisits(totalVisits)
                .lastVisitAt(lastVisit)
                .outstandingAmountMinor(outstanding)
                .build();

        return dtoMapper.toMemberProfile(member, subscription, stats);
    }

    private String nextMemberCode(Long branchId) {
        MemberSequence sequence = memberSequenceRepository.findByBranchIdForUpdate(branchId)
                .orElseGet(() -> MemberSequence.builder().branchId(branchId).seqValue(0).build());
        long next = sequence.getSeqValue() + 1;
        sequence.setSeqValue(next);
        memberSequenceRepository.save(sequence);
        int year = LocalDate.now().getYear();
        return String.format("M-%d-%05d", year, next);
    }

    /**
     * Blocks a second active member in the same organization with the same stored {@code phone}.
     * <p>Requires deterministic encryption (or plaintext): the same mobile number must always produce the
     * same ciphertext so equality matches in the database.</p>
     */
    private void assertNoDuplicatePhone(Long organizationId, Long excludeMemberId, String phone) {
        if (phone == null || phone.isBlank()) {
            throw new BusinessRuleException("Phone is required.");
        }
        if (memberRepository.countActiveByOrganizationIdAndPhoneExcluding(
                        organizationId, excludeMemberId, phone)
                > 0) {
            throw new BusinessRuleException(
                    "A member with this phone number is already registered for your organization.");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
