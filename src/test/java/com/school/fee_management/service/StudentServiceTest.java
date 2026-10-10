package com.school.fee_management.service;

import com.school.fee_management.dto.StudentRequest;
import com.school.fee_management.dto.StudentResponse;
import com.school.fee_management.entity.Student;
import com.school.fee_management.enums.StudentStatus;
import com.school.fee_management.exception.BadRequestException;
import com.school.fee_management.exception.DuplicateResourceException;
import com.school.fee_management.repository.FeePaymentRepository;
import com.school.fee_management.repository.StudentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private FeePaymentRepository feePaymentRepository;

    @InjectMocks
    private StudentService studentService;

    private final StudentRequest request =
            new StudentRequest("ADM001", "Rahul Sharma", "8-A", new BigDecimal("50000.00"));

    @Test
    void createStudent_validRequest_savesAsActive() {
        when(studentRepository.existsByAdmissionNumber("ADM001")).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StudentResponse response = studentService.createStudent(request);

        assertEquals("Rahul Sharma", response.name());
        assertEquals(StudentStatus.ACTIVE, response.status());
    }

    @Test
    void createStudent_duplicateAdmissionNumber_throwsAndDoesNotSave() {
        when(studentRepository.existsByAdmissionNumber("ADM001")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> studentService.createStudent(request));

        verify(studentRepository, never()).save(any());
    }

    @Test
    void updateStudent_feeLowerThanAmountPaid_throws() {
        Student student = new Student("ADM001", "Rahul Sharma", "8-A", new BigDecimal("50000.00"));
        when(studentRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(student));
        when(studentRepository.existsByAdmissionNumberAndIdNot("ADM001", 1L)).thenReturn(false);
        when(feePaymentRepository.sumAmountByStudentId(1L)).thenReturn(new BigDecimal("30000.00"));

        StudentRequest lowerFee = new StudentRequest("ADM001", "Rahul Sharma", "8-A", new BigDecimal("20000.00"));

        assertThrows(BadRequestException.class, () -> studentService.updateStudent(1L, lowerFee));
        assertEquals(0, new BigDecimal("50000.00").compareTo(student.getAnnualFee()));   // unchanged
    }

    @Test
    void deactivateStudent_setsStatusInactive_withoutDeleting() {
        Student student = new Student("ADM001", "Rahul Sharma", "8-A", new BigDecimal("50000.00"));
        when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

        studentService.deactivateStudent(1L);

        assertFalse(student.isActive());
        verify(studentRepository, never()).delete(any());
    }
}