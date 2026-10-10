package com.school.fee_management.entity;

import com.school.fee_management.enums.StudentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "students",
        check = @CheckConstraint(name = "chk_students_annual_fee", constraint = "annual_fee >= 0"))
@Getter
@NoArgsConstructor


public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "admission_number", nullable = false, unique = true, length = 30)
    private String admissionNumber;

    @Setter
    @Column(nullable = false, length = 100)
    private String name;

    @Setter
    @Column(name = "class_name", nullable = false, length = 20)
    private String className;

    @Setter
    @Column(name = "annual_fee", nullable = false, precision = 12, scale = 2)
    private BigDecimal annualFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private StudentStatus status = StudentStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Student(String admissionNumber, String name, String className, BigDecimal annualFee) {
        this.admissionNumber = admissionNumber;
        this.name = name;
        this.className = className;
        this.annualFee = annualFee;
    }

    public boolean isActive() {
        return status == StudentStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = StudentStatus.INACTIVE;
    }

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
