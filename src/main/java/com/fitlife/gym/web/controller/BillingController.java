package com.fitlife.gym.web.controller;

import com.fitlife.gym.service.BillingService;
import com.fitlife.gym.web.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;

    @GetMapping("/api/v1/invoices")
    public List<InvoiceDto> listInvoices(@RequestParam(required = false) Long branchId) {
        return billingService.listInvoices(branchId);
    }

    @GetMapping("/api/v1/payments")
    public List<PaymentDto> listPayments(@RequestParam(required = false) Long branchId) {
        return billingService.listPayments(branchId);
    }

    @PostMapping("/api/v1/payments")
    public PaymentDto recordPayment(@Valid @RequestBody RecordPaymentRequest request) {
        return billingService.recordPayment(request);
    }

    @GetMapping("/api/v1/payments/razorpay/config")
    public RazorpayConfigDto razorpayConfig() {
        return billingService.getRazorpayConfig();
    }

    @PostMapping("/api/v1/payments/razorpay/order")
    public CreateRazorpayOrderResponse createRazorpayOrder(
            @Valid @RequestBody CreateRazorpayOrderRequest request) {
        return billingService.createRazorpayOrder(request);
    }

    @PostMapping("/api/v1/payments/razorpay/confirm")
    public PaymentDto confirmRazorpay(
            @Valid @RequestBody ConfirmRazorpayPaymentRequest request) {
        return billingService.confirmRazorpayPayment(request);
    }
}
