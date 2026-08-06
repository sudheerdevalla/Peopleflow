package com.hr.hrapp.scheduler;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(PayrollScheduler.class);

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

        logger.info("AUTO PAYROLL STARTED");

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

                logger.info("PAYROLL SENT TO : {}", employee.getEmail());

            } catch (Exception e) {

                logger.error("FAILED FOR : {}", employee.getEmail(), e);
            }
        }

        logger.info("AUTO PAYROLL COMPLETED");
    }

    // =========================
    // CEO CONSOLIDATED REPORT
    // 1ST DAY OF MONTH - 12 AM
    // =========================

    @Scheduled(cron = "0 0 0 1 * ?")
    public void sendCEOReport() {

        try {

            ceoReportService.sendCEOReport();
            logger.info("CEO REPORT SENT");

        } catch (Exception e) {
            logger.error("Failed to send CEO report", e);
        }
    }
}