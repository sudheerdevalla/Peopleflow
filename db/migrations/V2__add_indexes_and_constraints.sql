-- Add indexes and constraints recommended by performance review

-- Employee
CREATE INDEX IF NOT EXISTS idx_employee_email ON employee(email);
CREATE INDEX IF NOT EXISTS idx_employee_status ON employee(status);
CREATE INDEX IF NOT EXISTS idx_employee_manager_id ON employee(manager_id);
CREATE INDEX IF NOT EXISTS idx_employee_department ON employee(department);
CREATE INDEX IF NOT EXISTS idx_employee_status_dept ON employee(status, department);

-- Leave (table name: leaves)
CREATE INDEX IF NOT EXISTS idx_leave_empId ON leaves(empId);
CREATE INDEX IF NOT EXISTS idx_leave_status ON leaves(status);
CREATE INDEX IF NOT EXISTS idx_leave_date ON leaves(date);
CREATE INDEX IF NOT EXISTS idx_leave_empId_status_date ON leaves(empId, status, date);

-- Timesheet
CREATE INDEX IF NOT EXISTS idx_timesheet_employeeId ON timesheet(employeeId);
CREATE INDEX IF NOT EXISTS idx_timesheet_date ON timesheet(date);
CREATE INDEX IF NOT EXISTS idx_timesheet_status ON timesheet(status);
CREATE INDEX IF NOT EXISTS idx_timesheet_empId_date ON timesheet(employeeId, date);

-- Salary
CREATE INDEX IF NOT EXISTS idx_salary_employeeId ON salary(employeeId);
CREATE INDEX IF NOT EXISTS idx_salary_month ON salary(month);

-- Candidate
CREATE INDEX IF NOT EXISTS idx_candidate_status ON candidate(status);
CREATE INDEX IF NOT EXISTS idx_candidate_email ON candidate(email);

-- Notification
CREATE INDEX IF NOT EXISTS idx_notification_employeeId ON notification(employeeId);
CREATE INDEX IF NOT EXISTS idx_notification_isRead ON notification(isRead);
CREATE INDEX IF NOT EXISTS idx_notification_createdAt ON notification(createdAt);
CREATE INDEX IF NOT EXISTS idx_notification_empId_isRead ON notification(employeeId, isRead);

-- AuditLog
CREATE INDEX IF NOT EXISTS idx_auditlog_timestamp ON audit_log(timestamp);
CREATE INDEX IF NOT EXISTS idx_auditlog_username ON audit_log(username);

-- TravelRequest
CREATE INDEX IF NOT EXISTS idx_travelrequest_empId ON travel_request(empId);
CREATE INDEX IF NOT EXISTS idx_travelrequest_status ON travel_request(status);
CREATE INDEX IF NOT EXISTS idx_travelrequest_empId_status ON travel_request(empId, status);

-- EmployeeAttendance
CREATE INDEX IF NOT EXISTS idx_attendance_employeeId ON attendance(employeeId);

-- EmployeeDocument
CREATE INDEX IF NOT EXISTS idx_emdoc_employeeId ON employee_document(employeeId);

-- Note: IF NOT EXISTS is supported by several DBs (Postgres, MySQL 8+). Adjust syntax for your DB.
