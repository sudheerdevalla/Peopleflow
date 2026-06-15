package com.hr.hrapp.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hr.hrapp.entity.Timesheet;

public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {

    // weekly data
    List<Timesheet> findByEmployeeIdAndDateBetween(
            Long employeeId,
            LocalDate start,
            LocalDate end
    );

    // single day
    Optional<Timesheet> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    
    List<Timesheet> findByEmployeeId(
            Long employeeId);
    
    List<Timesheet>
    findByStatus(
            String status);
    
    boolean existsByEmployeeIdAndDate(
            Long employeeId,
            LocalDate date);
}