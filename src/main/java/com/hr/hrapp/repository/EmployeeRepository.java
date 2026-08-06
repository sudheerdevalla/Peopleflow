package com.hr.hrapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hr.hrapp.entity.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Page<Employee> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Employee> findByStatus(String status);

    Page<Employee> findByStatus(String status, Pageable pageable);

    Employee findByEmail(String email);

    Employee findByEmpId(Long empId);
    
    // ✅ NEW: JOIN FETCH to prevent N+1 query when accessing manager
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.manager WHERE e.empId = :empId")
    Optional<Employee> findByEmpIdWithManager(@Param("empId") Long empId);
    
    // ✅ NEW: JOIN FETCH for email lookups
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.manager WHERE e.email = :email")
    Optional<Employee> findByEmailWithManager(@Param("email") String email);

    List<Employee> findByManager_EmpId(Long empId);

    List<Employee> findByManagerIsNull();

    long countByStatus(String status);

    long countByDepartment(String department);
}