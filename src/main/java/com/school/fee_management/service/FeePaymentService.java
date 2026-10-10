package com.school.fee_management.service;

import com.school.fee_management.dto.FeeSummaryResponse;
import com.school.fee_management.dto.PaymentRequest;
import com.school.fee_management.dto.PaymentResponse;
import com.school.fee_management.dto.RecordPaymentResponse;
import com.school.fee_management.entity.FeePayment;
import com.school.fee_management.entity.Student;
import com.school.fee_management.exception.BadRequestException;
import com.school.fee_management.exception.ResourceNotFoundException;
import com.school.fee_management.repository.FeePaymentRepository;
import com.school.fee_management.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FeePaymentService {

    private final StudentRepository studentRepository;
    private final FeePaymentRepository feePaymentRepository;

    @Transactional
    public RecordPaymentResponse recordPayment(Long studentId, PaymentRequest request) {

        Student student = studentRepository.findByIdForUpdate(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));

        if (!student.isActive()) {
            throw new BadRequestException("Cannot record payment for an inactive student");
        }

        BigDecimal totalPaid = feePaymentRepository.sumAmountByStudentId(studentId);
        BigDecimal balance = student.getAnnualFee().subtract(totalPaid);

        if (request.amount().compareTo(balance) > 0) {
            throw new BadRequestException("Payment of " + request.amount()
                    + " exceeds outstanding balance of " + balance);
        }

        LocalDate paymentDate = request.paymentDate() != null ? request.paymentDate() : LocalDate.now();
        FeePayment payment = new FeePayment(student, generateReceiptNumber(),
                request.amount(), request.paymentMethod(), paymentDate);
        FeePayment saved = feePaymentRepository.save(payment);

        FeeSummaryResponse summary = FeeSummaryResponse.of(student, totalPaid.add(request.amount()));
        return new RecordPaymentResponse(PaymentResponse.from(saved), summary);
    }

    @Transactional(readOnly = true)
    public FeeSummaryResponse getFeeSummary(Long studentId) {
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + studentId));
        BigDecimal totalPaid = feePaymentRepository.sumAmountByStudentId(studentId);
        return FeeSummaryResponse.of(student, totalPaid);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> getPaymentHistory(Long studentId, Pageable pageable) {
        if (!studentRepository.existsById(studentId)) {
            throw new ResourceNotFoundException("Student not found with id: " + studentId);
        }

        Pageable newestFirst = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "paymentDate", "id"));
        return feePaymentRepository.findByStudentId(studentId, newestFirst)
                .map(PaymentResponse::from);
    }

    private String generateReceiptNumber() {
        String date = LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        String random = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        return "RCPT-" + date + "-" + random;   // e.g. RCPT-20261010-A1B2C3
    }
}