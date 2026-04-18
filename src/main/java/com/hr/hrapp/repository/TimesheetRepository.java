package com.hr.hrapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hr.hrapp.entity.Timesheet;

import java.time.LocalDate;
import java.util.List;

public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {

    List<Timesheet> findByEmployeeIdAndDateBetween(
            Long employeeId,
            LocalDate start,
            LocalDate end
    );
}