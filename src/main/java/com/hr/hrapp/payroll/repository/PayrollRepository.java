package com.hr.hrapp.payroll.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hr.hrapp.payroll.entity.Payroll;

@Repository
public interface PayrollRepository
        extends JpaRepository<Payroll, Long> {

    Payroll findTopByEmployeeIdOrderByIdDesc(
            Long employeeId);

    List<Payroll> findByEmployeeIdOrderByIdDesc(
            Long employeeId);
    
    

    Optional<Payroll> findByEmployeeIdAndMonth(
            Long employeeId,
            String month);
    
    List<Payroll> findByMonth(
            String month);
}