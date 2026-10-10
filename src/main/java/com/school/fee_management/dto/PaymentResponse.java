package com.school.fee_management.dto;

import com.school.fee_management.entity.FeePayment;
import com.school.fee_management.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record PaymentResponse(
        Long id,
        String receiptNumber,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        LocalDate paymentDate,
        LocalDateTime createdAt
) {
    public static PaymentResponse from(FeePayment payment) {
        return new PaymentResponse(
                payment.getId(),
                payment.getReceiptNumber(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getPaymentDate(),
                payment.getCreatedAt()
        );
    }
}
