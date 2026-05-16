package com.fitlife.gym.service;

import com.fitlife.gym.domain.entity.*;
import com.fitlife.gym.domain.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fitlife.gym.exception.ResourceNotFoundException;
import com.fitlife.gym.security.GymUserPrincipal;
import com.fitlife.gym.web.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final PaymentRepository paymentRepository;
    private final MemberRepository memberRepository;
    private final BranchRepository branchRepository;
    private final BranchContextHelper branchContext;
    private final DtoMapper dtoMapper;
    private final RazorpayService razorpayService;

    @Transactional(readOnly = true)
    public List<InvoiceDto> listInvoices(Long branchId) {
        GymUserPrincipal user = branchContext.currentUser();
        Long effectiveBranchId = branchContext.resolveBranchIdOptional(branchId);
        return invoiceRepository.findByOrganizationAndBranch(user.getOrganizationId(), effectiveBranchId).stream()
                .map(dtoMapper::toInvoiceDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentDto> listPayments(Long branchId) {
        GymUserPrincipal user = branchContext.currentUser();
        Long effectiveBranchId = branchContext.resolveBranchIdOptional(branchId);
        return paymentRepository.findByOrganizationAndBranch(user.getOrganizationId(), effectiveBranchId).stream()
                .map(dtoMapper::toPaymentDto)
                .toList();
    }

    @Transactional
    public PaymentDto recordPayment(RecordPaymentRequest request) {
        GymUserPrincipal user = branchContext.currentUser();
        branchContext.assertBranchAccess(user, request.getBranchId());

        var existing = paymentRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (existing.isPresent()) {
            return dtoMapper.toPaymentDto(existing.get());
        }

        Member member = memberRepository.findByIdAndOrganizationId(request.getMemberId(), user.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        Branch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new ResourceNotFoundException("Branch not found"));

        if (request.getInvoiceId() != null) {
            Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
            long newPaid = invoice.getAmountPaidMinor() + request.getAmountMinor();
            invoice.setAmountPaidMinor(newPaid);
            if (newPaid >= invoice.getTotalMinor()) {
                invoice.setStatus("PAID");
            } else if (newPaid > 0) {
                invoice.setStatus("PARTIAL");
            }
            invoiceRepository.save(invoice);
        }

        String paymentNumber = generatePaymentNumber(branch.getCode(), user.getOrganizationId());
        Payment payment = Payment.builder()
                .organization(branch.getOrganization())
                .branch(branch)
                .member(member)
                .paymentNumber(paymentNumber)
                .amountMinor(request.getAmountMinor())
                .currencyCode(branch.getOrganization().getCurrencyCode())
                .method(request.getMethod())
                .status("SUCCESS")
                .idempotencyKey(request.getIdempotencyKey())
                .paidAt(Instant.now())
                .notes(request.getNotes())
                .receivedByUserId(user.getUserId())
                .build();

        payment = paymentRepository.save(payment);
        return dtoMapper.toPaymentDto(payment);
    }

    @Transactional(readOnly = true)
    public RazorpayConfigDto getRazorpayConfig() {
        boolean on = razorpayService.isConfigured();
        return RazorpayConfigDto.builder()
                .enabled(on)
                .keyId(on ? razorpayService.getPublishableKeyId() : null)
                .build();
    }

    @Transactional
    public CreateRazorpayOrderResponse createRazorpayOrder(CreateRazorpayOrderRequest request) {
        if (!razorpayService.isConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Razorpay is not configured");
        }
        GymUserPrincipal user = branchContext.currentUser();
        branchContext.assertBranchAccess(user, request.getBranchId());

        memberRepository
                .findByIdAndOrganizationId(request.getMemberId(), user.getOrganizationId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        String receipt = buildRazorpayReceipt(request.getMemberId());
        JsonNode order = razorpayService.createOrder(request.getAmountMinor(), receipt);
        return CreateRazorpayOrderResponse.builder()
                .keyId(razorpayService.getPublishableKeyId())
                .orderId(order.get("id").asText())
                .amount(order.get("amount").asLong())
                .currency(order.get("currency").asText("INR"))
                .build();
    }

    @Transactional
    public PaymentDto confirmRazorpayPayment(ConfirmRazorpayPaymentRequest request) {
        if (!razorpayService.isConfigured()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Razorpay is not configured");
        }
        if (!razorpayService.verifySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid Razorpay signature");
        }
        JsonNode pay = razorpayService.fetchPayment(request.getRazorpayPaymentId());
        String status = pay.path("status").asText("");
        if (!"captured".equals(status)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Payment not captured: " + status);
        }
        long amount = pay.path("amount").asLong(0L);
        if (amount != request.getAmountMinor()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment amount mismatch");
        }
        String orderOnRecord = pay.path("order_id").asText("");
        if (!request.getRazorpayOrderId().equals(orderOnRecord)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order id mismatch");
        }

        String noteExtra = "Razorpay "
                + request.getRazorpayPaymentId()
                + " order="
                + request.getRazorpayOrderId();
        String combinedNotes =
                request.getNotes() == null || request.getNotes().isBlank()
                        ? noteExtra
                        : request.getNotes().trim() + " | " + noteExtra;

        RecordPaymentRequest record = new RecordPaymentRequest();
        record.setMemberId(request.getMemberId());
        record.setBranchId(request.getBranchId());
        record.setAmountMinor(request.getAmountMinor());
        record.setMethod("ONLINE");
        record.setNotes(combinedNotes);
        record.setIdempotencyKey(request.getIdempotencyKey());
        return recordPayment(record);
    }

    private static String buildRazorpayReceipt(long memberId) {
        String base = "m" + memberId + "-" + UUID.randomUUID().toString().replace("-", "");
        return base.length() <= 40 ? base : base.substring(0, 40);
    }

    private String generatePaymentNumber(String branchCode, Long orgId) {
        long seq = paymentRepository.maxIdForOrg(orgId) + 1;
        int year = java.time.LocalDate.now().getYear();
        String code = branchCode != null ? branchCode.replace("BR-", "B") : "B0";
        return String.format("PAY-%s-%d-%05d", code, year, seq);
    }
}
