package com.hr.hrapp.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.Leave;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.repository.LeaveRepository;

@Service
public class LeaveService {

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private LeaveRepository leaveRepository;

    public void accrueLeaves(Employee emp) {

        LocalDate today = LocalDate.now();

        if (emp.getLastAccrualDate() == null) {

            if (emp.getJoiningDate() != null) {
                emp.setLastAccrualDate(emp.getJoiningDate());
            } else {
                // Cannot calculate accrual without a valid starting date.
                emp.setLastAccrualDate(today);
                employeeRepository.save(emp);
                return;
            }
        }

        // Never allow negative balance
        if (emp.getAnnualLeaves() < 0) {
            emp.setAnnualLeaves(0);
        }

        // Annual leave entitlement must never exceed 24 days
        if (emp.getAnnualLeaves() > 24) {
            emp.setAnnualLeaves(24);
            employeeRepository.save(emp);
        }

        long days = ChronoUnit.DAYS.between(
                emp.getLastAccrualDate(),
                today);

        if (days > 0) {

            YearMonth ym = YearMonth.now();

            double perDay = 2.0 / ym.lengthOfMonth();

            double newBalance =
                    emp.getAnnualLeaves()
                            + (days * perDay);

            // Maximum annual leave balance = 24 days
            emp.setAnnualLeaves(
                    Math.min(newBalance, 24.0));

            emp.setLastAccrualDate(today);

            employeeRepository.save(emp);
        }
    }
    public void applyLeave(Leave leave) {

        // Save leave
        leaveRepository.save(leave);

        // Get employee (fetch manager with JOIN FETCH to avoid N+1)
        Employee employee = employeeRepository
                .findByEmpIdWithManager(leave.getEmpId())
                .orElseThrow();

        // Get manager (already fetched)
        Employee manager = employee.getManager();

        // Send mail to manager
        if (manager != null) {

            String subject = "Leave Request Approval";

            String body =
                    "Employee " + employee.getName() +
                    " applied for leave on " + leave.getDate() +
                    ". Please approve or reject.";

            emailService.sendMail(
                    manager.getEmail(),
                    subject,
                    body
            );
        }
    }
}
