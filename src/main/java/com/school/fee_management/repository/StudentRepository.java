package com.school.fee_management.repository;

import com.school.fee_management.entity.Student;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByAdmissionNumber(String admissionNumber);

    boolean existsByAdmissionNumberAndIdNot(String admissionNumber, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Student s where s.id = :id")
    Optional<Student> findByIdForUpdate(@Param("id") Long id);

    @Query("select s from Student s " +
            "where lower(s.name) like lower(concat('%', :query, '%')) " +
            "or s.admissionNumber = :query")
    Page<Student> search(@Param("query") String query, Pageable pageable);
}