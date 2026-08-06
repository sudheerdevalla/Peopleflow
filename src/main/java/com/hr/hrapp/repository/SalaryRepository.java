package com.hr.hrapp.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hr.hrapp.entity.Salary;

import java.util.List;

public interface SalaryRepository extends JpaRepository<Salary, Long> {

    // Paginated versions for production usage
    Page<Salary> findByEmployeeId(Long employeeId, Pageable pageable);
    Page<Salary> findByMonth(String month, Pageable pageable);

    // Backwards-compatible list versions
    List<Salary> findByEmployeeId(Long employeeId);
    Salary findByMonth(String month);

}
