package com.school.fee_management.dto;

import com.school.fee_management.entity.Student;
import com.school.fee_management.enums.FeeStatus;

import java.math.BigDecimal;

public record FeeSummaryResponse(
        Long studentId,
        String studentName,
        BigDecimal annualFee,
        BigDecimal totalPaid,
        BigDecimal balance,
        FeeStatus status
) {
    public static FeeSummaryResponse of(Student student, BigDecimal totalPaid) {
        BigDecimal balance = student.getAnnualFee().subtract(totalPaid);

        FeeStatus status;
        if (balance.compareTo(BigDecimal.ZERO) <= 0) {
            status = FeeStatus.PAID;
        } else if (totalPaid.compareTo(BigDecimal.ZERO) == 0) {
            status = FeeStatus.UNPAID;
        } else {
            status = FeeStatus.PARTIALLY_PAID;
        }

        return new FeeSummaryResponse(student.getId(), student.getName(),
                student.getAnnualFee(), totalPaid, balance, status);
    }
}
