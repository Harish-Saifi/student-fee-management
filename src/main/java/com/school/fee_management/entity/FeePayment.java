package com.school.fee_management.entity;


import com.school.fee_management.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "fee_payments",
        indexes = @Index(name = "idx_fee_payments_student_id", columnList = "student_id"),
        check = @CheckConstraint(name = "chk_fee_payments_amount", constraint = "amount > 0"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FeePayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @Column(name = "receipt_number", nullable = false, unique = true, length = 30)
    private String receiptNumber;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    private PaymentMethod paymentMethod;

    @Column(name = "payment_date", nullable = false)
    private LocalDate paymentDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public FeePayment(Student student, String receiptNumber, BigDecimal amount,
                      PaymentMethod paymentMethod, LocalDate paymentDate) {
        this.student = student;
        this.receiptNumber = receiptNumber;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.paymentDate = paymentDate;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}