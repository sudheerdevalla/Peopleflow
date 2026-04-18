package com.hr.hrapp.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hr.hrapp.entity.Salary;

import java.util.List;

public interface SalaryRepository extends JpaRepository<Salary, Long> {

    List<Salary> findByEmployeeId(Long employeeId);

}
