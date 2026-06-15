package com.hr.hrapp.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.payroll.entity.Payroll;
import com.hr.hrapp.payroll.report.CEOReportService;
import com.hr.hrapp.payroll.service.PayrollMailService;
import com.hr.hrapp.payroll.service.PayrollService;
import com.hr.hrapp.repository.EmployeeRepository;

@Component
public class PayrollScheduler {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private PayrollService payrollService;

    @Autowired
    private PayrollMailService payrollMailService;

    @Autowired
    private CEOReportService ceoReportService;

    // =========================
    // EMPLOYEE PAYROLL MAILS
    // LAST DAY OF MONTH - 11 PM
    // =========================

   
   // @Scheduled(cron = "0 */2 * * * ?")
    
    @Scheduled(cron = "0 0 23 L * ?")
    public void autoGeneratePayroll() {

        System.out.println("AUTO PAYROLL STARTED");

        List<Employee> employees =
                employeeRepository.findAll();

        for(Employee employee : employees) {

            try {

                Payroll payroll =
                        payrollService
                        .calculateSalary(employee);

                payrollMailService.sendPayslip(
                        payroll,
                        employee.getEmail());

                System.out.println(
                        "PAYROLL SENT TO : "
                        + employee.getEmail());

            } catch (Exception e) {

                System.out.println(
                        "FAILED FOR : "
                        + employee.getEmail());

                e.printStackTrace();
            }
        }

        System.out.println(
                "AUTO PAYROLL COMPLETED");
    }

    // =========================
    // CEO CONSOLIDATED REPORT
    // 1ST DAY OF MONTH - 12 AM
    // =========================

    @Scheduled(cron = "0 0 0 1 * ?")
    public void sendCEOReport() {

        try {

            ceoReportService.sendCEOReport();

            System.out.println(
                    "CEO REPORT SENT");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}