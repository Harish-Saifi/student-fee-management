package com.school.fee_management.repository;

import com.school.fee_management.entity.FeePayment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface FeePaymentRepository extends JpaRepository<FeePayment, Long> {


    @Query("select coalesce(sum(p.amount), 0) from FeePayment p where p.student.id = :studentId")
    BigDecimal sumAmountByStudentId(@Param("studentId") Long studentId);

    Page<FeePayment> findByStudentId(Long studentId, Pageable pageable);
}