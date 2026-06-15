
package com.hr.hrapp.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hr.hrapp.entity.Employee;

//public interface EmployeeRepository extends JpaRepository<Employee, Integer>
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Page<Employee> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Employee> findByStatus(String status);

    Page<Employee> findByStatus(String status, Pageable pageable);

    Employee findByEmail(String email);

    Employee findByEmpId(Long empId);

    List<Employee> findByManager_EmpId(Long empId);

    List<Employee> findByManagerIsNull();

    long countByStatus(String status);

    long countByDepartment(String department);
}