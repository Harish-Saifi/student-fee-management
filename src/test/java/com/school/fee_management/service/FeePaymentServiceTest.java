package com.school.fee_management.service;

import static org.junit.jupiter.api.Assertions.*;

import com.school.fee_management.dto.FeeSummaryResponse;
import com.school.fee_management.dto.PaymentRequest;
import com.school.fee_management.dto.RecordPaymentResponse;
import com.school.fee_management.entity.FeePayment;
import com.school.fee_management.entity.Student;
import com.school.fee_management.enums.FeeStatus;
import com.school.fee_management.enums.PaymentMethod;
import com.school.fee_management.exception.BadRequestException;
import com.school.fee_management.exception.ResourceNotFoundException;
import com.school.fee_management.repository.FeePaymentRepository;
import com.school.fee_management.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FeePaymentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FeePaymentRepository feePaymentRepository;

    @InjectMocks
    private FeePaymentService feePaymentService;

    private Student student;

    @BeforeEach
    void setUp() {
        student = new Student("ADM001", "Rahul Sharma", "8-A", new BigDecimal("50000.00"));
    }

    @Test
    void recordPayment_partialPayment_statusIsPartiallyPaid() {
        when(studentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(student));
        when(feePaymentRepository.sumAmountByStudentId(1L)).thenReturn(BigDecimal.ZERO);
        when(feePaymentRepository.save(any(FeePayment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecordPaymentResponse response = feePaymentService.recordPayment(1L,
                new PaymentRequest(new BigDecimal("20000.00"), PaymentMethod.UPI, null));

        assertEquals(FeeStatus.PARTIALLY_PAID, response.feeSummary().status());
        assertEquals(0, new BigDecimal("30000.00").compareTo(response.feeSummary().balance()));
        assertEquals(LocalDate.now(), response.payment().paymentDate());
    }

    @Test
    void recordPayment_exactRemainingBalance_statusIsPaid() {
        when(studentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(student));
        when(feePaymentRepository.sumAmountByStudentId(1L)).thenReturn(new BigDecimal("30000.00"));
        when(feePaymentRepository.save(any(FeePayment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RecordPaymentResponse response = feePaymentService.recordPayment(1L,
                new PaymentRequest(new BigDecimal("20000.00"), PaymentMethod.CASH, null));

        assertEquals(FeeStatus.PAID, response.feeSummary().status());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.feeSummary().balance()));
    }

    @Test
    void recordPayment_amountMoreThanBalance_throwsAndDoesNotSave() {
        when(studentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(student));
        when(feePaymentRepository.sumAmountByStudentId(1L)).thenReturn(new BigDecimal("45000.00"));

        assertThrows(BadRequestException.class, () -> feePaymentService.recordPayment(1L,
                new PaymentRequest(new BigDecimal("10000.00"), PaymentMethod.UPI, null)));

        verify(feePaymentRepository, never()).save(any());
    }

    @Test
    void recordPayment_fullyPaidStudent_throws() {
        when(studentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(student));
        when(feePaymentRepository.sumAmountByStudentId(1L)).thenReturn(new BigDecimal("50000.00"));

        assertThrows(BadRequestException.class, () -> feePaymentService.recordPayment(1L,
                new PaymentRequest(new BigDecimal("1.00"), PaymentMethod.CASH, null)));
    }

    @Test
    void recordPayment_inactiveStudent_throws() {
        student.deactivate();
        when(studentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(student));

        assertThrows(BadRequestException.class, () -> feePaymentService.recordPayment(1L,
                new PaymentRequest(new BigDecimal("1000.00"), PaymentMethod.CASH, null)));

        verify(feePaymentRepository, never()).save(any());
    }

    @Test
    void recordPayment_studentNotFound_throws() {
        when(studentRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> feePaymentService.recordPayment(99L,
                new PaymentRequest(new BigDecimal("1000.00"), PaymentMethod.CASH, null)));
    }

    @Test
    void getFeeSummary_noPayments_statusIsUnpaid() {
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
        when(feePaymentRepository.sumAmountByStudentId(1L)).thenReturn(BigDecimal.ZERO);

        FeeSummaryResponse summary = feePaymentService.getFeeSummary(1L);

        assertEquals(FeeStatus.UNPAID, summary.status());
        assertEquals(0, new BigDecimal("50000.00").compareTo(summary.balance()));
    }
}