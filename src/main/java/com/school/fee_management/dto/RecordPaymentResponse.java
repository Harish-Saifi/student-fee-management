package com.school.fee_management.dto;

public record RecordPaymentResponse(
        PaymentResponse payment,
        FeeSummaryResponse feeSummary
) {
}