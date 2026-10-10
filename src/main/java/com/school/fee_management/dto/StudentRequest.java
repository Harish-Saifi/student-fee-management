package com.school.fee_management.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record StudentRequest(
        @NotBlank @Size(max = 30) String admissionNumber,
        @NotBlank @Size(max = 100) String name,
        @NotBlank @Size(max = 20) String className,
        @NotNull @PositiveOrZero @Digits(integer = 10, fraction = 2) BigDecimal annualFee
) {
}