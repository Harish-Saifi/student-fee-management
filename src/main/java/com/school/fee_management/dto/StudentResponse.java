package com.school.fee_management.dto;

import com.school.fee_management.enums.StudentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.school.fee_management.entity.Student;

public record StudentResponse(
        Long id,
        String admissionNumber,
        String name,
        String className,
        BigDecimal annualFee,
        StudentStatus status,
        LocalDateTime createdAt
) {
    public static StudentResponse from(Student student) {
        return new StudentResponse(
                student.getId(),
                student.getAdmissionNumber(),
                student.getName(),
                student.getClassName(),
                student.getAnnualFee(),
                student.getStatus(),
                student.getCreatedAt()
        );
    }
}