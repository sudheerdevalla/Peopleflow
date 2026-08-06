package com.hr.hrapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hr.hrapp.entity.EmployeeDocument;

@Repository
public interface EmployeeDocumentRepository
        extends JpaRepository<EmployeeDocument, Long> {

    // Paginated version
    Page<EmployeeDocument> findByEmployeeId(Long employeeId, Pageable pageable);

    // Backwards-compatible list version
    List<EmployeeDocument> findByEmployeeId(Long employeeId);
    Optional<EmployeeDocument> findById(Long id);

}