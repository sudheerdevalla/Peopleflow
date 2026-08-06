# HRMS Application - Spring Data JPA Performance Review

**Review Date:** June 24, 2026  
**Application:** HR Resource Management System  
**Focus:** Production Performance Optimization

---

## Executive Summary

**Critical Issues Found:** 5  
**High Priority Issues:** 8  
**Database Tables Analyzed:** 18  
**Repositories Analyzed:** 18  

---

## 🚨 CRITICAL ISSUES

### 1. N+1 Query - Employee Manager Relationship
**File:** LeaveService.java (Line 71)  
**Entity:** Employee  
**Issue:** Accessing manager without JOIN FETCH triggers separate query  
**SQL Impact:** 1 query to get employee + 1 query per access to manager

```java
// PROBLEM
Employee employee = employeeRepository.findById(leave.getEmpId()).orElseThrow();
Employee manager = employee.getManager();  // ❌ LAZY - Separate query
```

**Solution:** Add JOIN FETCH in repository

---

### 2. Missing Unique Constraint on Email
**File:** Employee.java (Line 31), User.java (Line 38), Candidate.java (Line 9)  
**Issue:** No unique constraint on email/username fields  
**Business Impact:** Duplicate emails could corrupt data integrity

```java
// PROBLEM
private String email;  // ❌ No unique constraint

// Candidate.java actually has it commented out!
//@Column(unique = true)
private String email;
```

---

### 3. Eager Fetch on User Roles
**File:** User.java (Line 16)  
**Issue:** EAGER fetch causes joined queries for every user load  
**SQL Impact:** Unnecessary joins, lazy init exceptions

```java
// PROBLEM
@ManyToMany(fetch = jakarta.persistence.FetchType.EAGER)  // ❌ EAGER
private Set<Role> roles;
```

---

### 4. Full Table Scans - No Pagination
**File:** EmployeeSalaryService.java (Line 91)  
**Issue:** getAllEmployees() loads ALL records without limit  
**SQL Impact:** SELECT * FROM employee with no LIMIT clause

```java
// PROBLEM
public List<Employee> getAllEmployees() {
    return repo.findAll();  // ❌ No pagination
}
```

---

### 5. Missing Indexes on Foreign Keys
**Tables Affected:** Employee, Leave, Timesheet, Salary, Notification, TravelRequest, etc.  
**Issue:** No database indexes on frequently searched foreign key columns  
**SQL Impact:** Table scans instead of index seeks

| Table | Missing Index | Query | Impact |
|-------|---------------|-------|--------|
| Leave | empId | findByEmpId() | Full scan |
| Timesheet | employeeId | findByEmployeeId() | Full scan |
| Salary | employeeId | findByEmployeeId() | Full scan |
| Notification | employeeId | findByEmployeeId() | Full scan |
| TravelRequest | empId | findByEmpId() | Full scan |

---

## ⚠️ HIGH PRIORITY ISSUES

### 6. No Pagination on List Queries
**Affected Repositories:**
- LeaveRepository.findByEmpId()
- TimesheetRepository.findByEmployeeId()
- SalaryRepository.findByEmployeeId()
- EmployeeDocumentRepository.findByEmployeeId()
- NotificationRepository.findByEmployeeIdOrderByCreatedAtDesc()

**Issue:** All results loaded into memory

---

### 7. Missing Composite Indexes
**Composite Indexes Needed:**
- Employee: (status, department)
- Leave: (empId, status, date)
- Timesheet: (employeeId, date)
- Notification: (employeeId, isRead)
- TravelRequest: (empId, status)

---

### 8. Missing Unique Composite Constraints
**Timesheet (employeeId, date):** Should be unique - prevents duplicate entries per day  
**Leave (empId, date, type):** Should be unique - prevents duplicate leaves

---

### 9. No Index on Timestamp Columns
**Affected Columns:**
- AuditLog.timestamp
- Notification.createdAt
- CompanyUpdate.createdAt

**Impact:** Sorting/filtering by date is slow

---

### 10. Inefficient Role/Permission Lazy Initialization
**File:** Role.java (Line 29)  
**Issue:** Role.permissions uses LAZY but accessed in loops  
**SQL Impact:** N queries when iterating permissions

---

### 11. Missing @EntityGraph Hints
**Issue:** No entity graphs defined for common access patterns  
**Solution:** Use @EntityGraph to optimize fetch strategies

---

### 12. Candidate Status Count Query Inefficiency
**File:** CandidateRepository.java (Line 18)  
**Issue:** Returns Object[], requires application-level casting  
**Solution:** Use native query or projection

---

### 13. Inefficient Distinct Queries
**Issue:** No specification of DISTINCT in join queries  
**SQL Impact:** Potential duplicate rows from joins

---

## 📊 SQL OPTIMIZATION RECOMMENDATIONS

### Create Missing Indexes (DDL)

```sql
-- Employee Table
CREATE INDEX idx_employee_email ON employee(email);
CREATE INDEX idx_employee_status ON employee(status);
CREATE INDEX idx_employee_manager_id ON employee(manager_id);
CREATE INDEX idx_employee_department ON employee(department);
CREATE INDEX idx_employee_status_dept ON employee(status, department);

-- Leave Table
CREATE INDEX idx_leave_empId ON leaves(empId);
CREATE INDEX idx_leave_status ON leaves(status);
CREATE INDEX idx_leave_date ON leaves(date);
CREATE INDEX idx_leave_empId_status_date ON leaves(empId, status, date);

-- Timesheet Table
CREATE INDEX idx_timesheet_employeeId ON timesheet(employeeId);
CREATE INDEX idx_timesheet_date ON timesheet(date);
CREATE INDEX idx_timesheet_status ON timesheet(status);
CREATE INDEX idx_timesheet_empId_date ON timesheet(employeeId, date);

-- Salary Table
CREATE INDEX idx_salary_employeeId ON salary(employeeId);
CREATE INDEX idx_salary_month ON salary(month);

-- Candidate Table
CREATE INDEX idx_candidate_status ON candidate(status);
CREATE INDEX idx_candidate_email ON candidate(email);

-- Notification Table
CREATE INDEX idx_notification_employeeId ON notification(employeeId);
CREATE INDEX idx_notification_isRead ON notification(isRead);
CREATE INDEX idx_notification_createdAt ON notification(createdAt);
CREATE INDEX idx_notification_empId_isRead ON notification(employeeId, isRead);

-- AuditLog Table
CREATE INDEX idx_auditlog_timestamp ON audit_log(timestamp);

-- TravelRequest Table
CREATE INDEX idx_travelrequest_empId ON travel_request(empId);
CREATE INDEX idx_travelrequest_status ON travel_request(status);
CREATE INDEX idx_travelrequest_empId_status ON travel_request(empId, status);

-- EmployeeAttendance Table
CREATE INDEX idx_attendance_employeeId ON attendance(employeeId);

-- EmployeeDocument Table
CREATE INDEX idx_emdoc_employeeId ON employee_document(employeeId);
```

---

## 🔧 DETAILED CODE CHANGES

### FIX #1: Add Unique Constraints to Entities

**Employee.java**
```java
// Line 31 - Change from:
private String email;

// To:
@Column(nullable = false, unique = true)
private String email;
```

**User.java**
```java
// Add unique constraint
@Column(unique = true, nullable = false)
private String username;
```

**Candidate.java**
```java
// Line 9 - Uncomment and fix:
@Column(unique = true, nullable = false)
private String email;
```

---

### FIX #2: Change User.roles from EAGER to LAZY

**User.java (Line 16)**
```java
// FROM:
@ManyToMany(fetch = jakarta.persistence.FetchType.EAGER)
@JoinTable(...)
private Set<Role> roles;

// TO:
@ManyToMany(fetch = jakarta.persistence.FetchType.LAZY)
@JoinTable(...)
private Set<Role> roles;
```

---

### FIX #3: Add JOIN FETCH for Manager Query

**EmployeeRepository.java**
```java
import org.springframework.data.jpa.repository.Query;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    // Existing methods...
    Page<Employee> findByNameContainingIgnoreCase(String name, Pageable pageable);
    List<Employee> findByStatus(String status);
    Page<Employee> findByStatus(String status, Pageable pageable);
    
    // NEW: Fetch manager eagerly
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.manager WHERE e.empId = :empId")
    Optional<Employee> findByEmpIdWithManager(@Param("empId") Long empId);
    
    // NEW: Alternative for Email lookup
    @Query("SELECT e FROM Employee e LEFT JOIN FETCH e.manager WHERE e.email = :email")
    Employee findByEmailWithManager(@Param("email") String email);
    
    Employee findByEmpId(Long empId);
    Employee findByEmail(String email);
    List<Employee> findByManager_EmpId(Long empId);
    List<Employee> findByManagerIsNull();
    long countByStatus(String status);
    long countByDepartment(String department);
}
```

**LeaveService.java (Update Line 66-71)**
```java
// FROM:
Employee employee = employeeRepository.findById(leave.getEmpId()).orElseThrow();

// TO:
Employee employee = employeeRepository.findByEmpIdWithManager(leave.getEmpId());
if (employee == null) {
    throw new EntityNotFoundException("Employee not found: " + leave.getEmpId());
}
```

---

### FIX #4: Add Pagination Support

**EmployeeSalaryService.java**
```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;

@Service
public class EmployeeSalaryService {
    
    // NEW: Paginated version
    public Page<Employee> getAllEmployeesPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repo.findAll(pageable);
    }
    
    // Keep existing but mark as deprecated
    @Deprecated(since = "2.0", forRemoval = true)
    public List<Employee> getAllEmployees() {
        return repo.findAll();
    }
    
    // ... rest of class ...
}
```

---

### FIX #5: Add Paginated Repository Methods

**LeaveRepository.java**
```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface LeaveRepository extends JpaRepository<Leave, Long> {
    
    // NEW: Paginated versions
    Page<Leave> findByEmpId(Long empId, Pageable pageable);
    Page<Leave> findByEmpIdAndStatus(Long empId, String status, Pageable pageable);
    
    // Keep existing for backward compatibility
    List<Leave> findByEmpId(Long empId);
    
    int countByEmpIdAndTypeAndStatus(Long empId, String type, String status);
    boolean existsByEmpIdAndDateAndStatus(Long empId, LocalDate date, String status);
    long countByStatus(String status);
}
```

**TimesheetRepository.java**
```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TimesheetRepository extends JpaRepository<Timesheet, Long> {
    
    // NEW: Paginated versions
    Page<Timesheet> findByEmployeeIdAndDateBetween(
        Long employeeId, LocalDate start, LocalDate end, Pageable pageable);
    
    Page<Timesheet> findByEmployeeId(Long employeeId, Pageable pageable);
    Page<Timesheet> findByStatus(String status, Pageable pageable);
    
    // Keep existing for backward compatibility
    List<Timesheet> findByEmployeeIdAndDateBetween(Long employeeId, LocalDate start, LocalDate end);
    List<Timesheet> findByEmployeeId(Long employeeId);
    List<Timesheet> findByStatus(String status);
    Optional<Timesheet> findByEmployeeIdAndDate(Long employeeId, LocalDate date);
    boolean existsByEmployeeIdAndDate(Long employeeId, LocalDate date);
}
```

**SalaryRepository.java**
```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SalaryRepository extends JpaRepository<Salary, Long> {
    
    // NEW: Paginated versions
    Page<Salary> findByEmployeeId(Long employeeId, Pageable pageable);
    Page<Salary> findByMonth(String month, Pageable pageable);
    
    // Keep existing for backward compatibility
    List<Salary> findByEmployeeId(Long employeeId);
    List<Salary> findByMonth(String month);
}
```

**NotificationRepository.java**
```java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // NEW: Paginated version
    Page<Notification> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId, Pageable pageable);
    
    // Keep existing for backward compatibility
    List<Notification> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);
    
    long countByEmployeeIdAndIsReadFalse(Long employeeId);
}
```

---

### FIX #6: Add Unique Composite Constraints

**Timesheet.java**
```java
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "timesheet", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"employeeId", "date"}, name = "uk_timesheet_empid_date")
})
public class Timesheet {
    // ... existing fields ...
}
```

**Leave.java**
```java
@Entity
@Table(name = "leaves", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"empId", "date", "type"}, name = "uk_leave_empid_date_type")
})
public class Leave {
    // ... existing fields ...
}
```

---

### FIX #7: Add Explicit Lazy Loading

**Employee.java (Line 127-130)**
```java
import jakarta.persistence.FetchType;

@JsonIgnore
@OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)  // ✅ Explicit
private List<Employee> subordinates = new ArrayList<>();
```

---

### FIX #8: Optimize Candidate Status Query

**CandidateRepository.java**
```java
import org.springframework.data.jpa.repository.Query;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    
    List<Candidate> findByStatus(String status);
    long countByStatus(String status);
    
    // Existing
    @Query("SELECT c.status, COUNT(c) FROM Candidate c GROUP BY c.status")
    List<Object[]> countCandidatesGroupByStatus();
    
    // NEW: Native query for better performance
    @Query(value = "SELECT status, COUNT(*) as count FROM candidate GROUP BY status", nativeQuery = true)
    List<Object[]> countCandidatesGroupByStatusNative();
}
```

---

## 📈 Expected Performance Improvements

| Issue | Before | After | Gain |
|-------|--------|-------|------|
| Email lookup | Full table scan | Index seek | 1000x faster |
| Manager access | N+1 queries | 1 query | N times faster |
| Employee list | Load 10K+ records | Paginate 10-50 | 100-1000x memory |
| Timesheet range query | Full scan | Index range | 100x faster |

---

## ✅ Implementation Checklist

- [ ] Execute SQL DDL for indexes
- [ ] Add @Column(unique=true) to emails
- [ ] Change User.roles to LAZY
- [ ] Add JOIN FETCH to Employee repository
- [ ] Create paginated methods
- [ ] Add unique constraints to Timesheet and Leave
- [ ] Update services to use pagination
- [ ] Update controllers to accept Pageable
- [ ] Add unit tests for N+1 prevention
- [ ] Enable SQL logging for validation

