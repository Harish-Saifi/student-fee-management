package com.school.fee_management.controller;

import com.school.fee_management.dto.FeeSummaryResponse;
import com.school.fee_management.dto.PaymentRequest;
import com.school.fee_management.dto.PaymentResponse;
import com.school.fee_management.dto.RecordPaymentResponse;
import com.school.fee_management.service.FeePaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students/{studentId}")
@RequiredArgsConstructor
public class FeePaymentController {

    private final FeePaymentService feePaymentService;

    @PostMapping("/payments")
    public ResponseEntity<RecordPaymentResponse> recordPayment(@PathVariable Long studentId,
                                                               @Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(feePaymentService.recordPayment(studentId, request));
    }

    @GetMapping("/payments")
    public Page<PaymentResponse> getPaymentHistory(@PathVariable Long studentId,
                                                   @PageableDefault(size = 10) Pageable pageable) {
        return feePaymentService.getPaymentHistory(studentId, pageable);
    }

    @GetMapping("/fee-summary")
    public FeeSummaryResponse getFeeSummary(@PathVariable Long studentId) {
        return feePaymentService.getFeeSummary(studentId);
    }
}