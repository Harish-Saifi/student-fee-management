package com.school.fee_management.service;

import com.school.fee_management.dto.StudentRequest;
import com.school.fee_management.dto.StudentResponse;
import com.school.fee_management.entity.Student;
import com.school.fee_management.exception.BadRequestException;
import com.school.fee_management.exception.DuplicateResourceException;
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

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final FeePaymentRepository feePaymentRepository;

    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        if (studentRepository.existsByAdmissionNumber(request.admissionNumber())) {
            throw new DuplicateResourceException("Admission number already exists: " + request.admissionNumber());
        }
        Student student = new Student(request.admissionNumber(), request.name(),
                request.className(), request.annualFee());
        return StudentResponse.from(studentRepository.save(student));
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudent(Long id) {
        return StudentResponse.from(findStudent(id));
    }

    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        Student student = studentRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));

        if (studentRepository.existsByAdmissionNumberAndIdNot(request.admissionNumber(), id)) {
            throw new DuplicateResourceException("Admission number already exists: " + request.admissionNumber());
        }

        BigDecimal totalPaid = feePaymentRepository.sumAmountByStudentId(id);
        if (request.annualFee().compareTo(totalPaid) < 0) {
            throw new BadRequestException("Annual fee cannot be less than the amount already paid: " + totalPaid);
        }

        student.setAdmissionNumber(request.admissionNumber());
        student.setName(request.name());
        student.setClassName(request.className());
        student.setAnnualFee(request.annualFee());
        return StudentResponse.from(student);
    }

    @Transactional
    public void deactivateStudent(Long id) {
        Student student = findStudent(id);
        student.deactivate();
    }

    @Transactional(readOnly = true)
    public Page<StudentResponse> searchStudents(String query, Pageable pageable) {
        Pageable sortedByName = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("name"));

        Page<Student> students = (query == null || query.isBlank())
                ? studentRepository.findAll(sortedByName)
                : studentRepository.search(query.trim(), sortedByName);
        return students.map(StudentResponse::from);
    }

    private Student findStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }
}