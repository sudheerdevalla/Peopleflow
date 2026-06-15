package com.hr.hrapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hr.hrapp.entity.EmployeeDocument;

@Repository
public interface EmployeeDocumentRepository
        extends JpaRepository<EmployeeDocument, Long> {

    List<EmployeeDocument> findByEmployeeId(Long employeeId);
    Optional<EmployeeDocument> findById(Long id);

}