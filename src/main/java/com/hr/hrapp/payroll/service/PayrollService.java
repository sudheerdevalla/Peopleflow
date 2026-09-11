package com.hr.hrapp.payroll.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.Timesheet;
import com.hr.hrapp.entity.TravelRequest;
import com.hr.hrapp.payroll.entity.Payroll;
import com.hr.hrapp.payroll.repository.PayrollRepository;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.repository.TimesheetRepository;
import com.hr.hrapp.repository.TravelRequestRepository;
import com.hr.hrapp.service.AuditTrailService;
import com.hr.hrapp.util.PayrollMonthUtil;

@Service
public class PayrollService {

    private static final String FINALIZED = "FINALIZED";

    private static final String DRAFT = "DRAFT";

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private TimesheetRepository timesheetRepository;
    
    @Autowired
    private TravelRequestRepository travelRepository;

    @Autowired
    private AuditTrailService auditTrailService;

    @Transactional
    public Payroll calculateSalary(Employee employee) {

        return calculateSalary(employee, YearMonth.now());
    }

    @Transactional
    public Payroll calculateSalary(Employee employee, YearMonth payrollMonth) {

        String monthLabel = PayrollMonthUtil.format(payrollMonth);
        Payroll existingPayroll = payrollRepository
                .findByEmployeeIdAndMonth(employee.getEmpId(), monthLabel)
                .orElse(null);

        if (existingPayroll != null && FINALIZED.equalsIgnoreCase(existingPayroll.getStatus())) {
            return existingPayroll;
        }

        LocalDate startDate = payrollMonth.atDay(1);
        LocalDate endDate = payrollMonth.atEndOfMonth();
        int workingDays = payrollMonth.lengthOfMonth();

        List<Timesheet> monthTimesheets = timesheetRepository
                .findByEmployeeIdAndDateBetween(employee.getEmpId(), startDate, endDate);

        int approvedTimesheetDays = (int) monthTimesheets.stream()
                .filter(t -> t.getStatus() != null && t.getStatus().equalsIgnoreCase("APPROVED"))
                .count();

        int payableDays = approvedTimesheetDays > 0 ? approvedTimesheetDays : workingDays;

        double fullMonthBasic = round(employee.getBasicSalary());
        double payableBasicSalary = round(fullMonthBasic * payableDays / Math.max(workingDays, 1));
        double hra = round(payableBasicSalary * safePercentage(employee.getHraPercentage()) / 100.0);
        double bonus = round(payableBasicSalary * safePercentage(employee.getBonusPercentage()) / 100.0);
        double approvedTravelAllowance = round(nullSafe(travelRepository.getApprovedTravelAllowance(employee.getEmpId())));
        double fixedTravelAllowance = round(nullSafe(employee.getTravelAllowance()));
        double totalTravelAllowance = round(fixedTravelAllowance + approvedTravelAllowance);
        double approvedAdditions = approvedTravelAllowance;
        double grossSalary = round(payableBasicSalary + hra + bonus + totalTravelAllowance);
        double pf = round(Math.min(payableBasicSalary, 15000.0) * 0.12);

        double annualGrossSalary = round(grossSalary * 12);

        double standardDeduction = 75000.0;

        double taxableIncome = Math.max(
                0.0,
                annualGrossSalary - standardDeduction);

        double annualTax = calculateNewRegimeTax(taxableIncome);

        double tax = round(annualTax / 12.0);

        double deductions = round(pf + tax);
        double netSalary = round(grossSalary - deductions);

        Payroll payroll = existingPayroll != null ? existingPayroll : new Payroll();
        payroll.setEmployeeId(employee.getEmpId());
        payroll.setEmployeeName(employee.getName());
        payroll.setBasicSalary(payableBasicSalary);
        payroll.setHra(hra);
        payroll.setBonus(bonus);
        payroll.setTravelAllowance(totalTravelAllowance);
        payroll.setApprovedAdditions(approvedAdditions);
        payroll.setGrossSalary(grossSalary);
        payroll.setPf(pf);
        payroll.setTax(tax);
        payroll.setDeductions(deductions);
        payroll.setNetSalary(netSalary);
        payroll.setPayableDays(payableDays);
        payroll.setWorkingDays(workingDays);
        payroll.setMonth(monthLabel);
        payroll.setStatus(DRAFT);
        payroll.setReconciliationStatus(netSalary >= 0 ? "RECONCILED" : "ERROR");
        payroll.setLastCalculatedAt(LocalDateTime.now());
        payroll.setFinalizedAt(null);
        payroll.setFinalizedBy(null);

        return payrollRepository.save(payroll);
    }

    @Transactional
    public Payroll finalizePayroll(Long employeeId, YearMonth payrollMonth, String finalizedBy) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        Payroll payroll = calculateSalary(employee, payrollMonth);
        if (!"RECONCILED".equalsIgnoreCase(payroll.getReconciliationStatus())) {
            throw new IllegalStateException("Payroll reconciliation failed; cannot finalize");
        }

        if (FINALIZED.equalsIgnoreCase(payroll.getStatus())) {
            return payroll;
        }

        payroll.setStatus(FINALIZED);
        payroll.setFinalizedAt(LocalDateTime.now());
        payroll.setFinalizedBy(finalizedBy);
        Payroll savedPayroll = payrollRepository.save(payroll);

        List<TravelRequest> travels = new ArrayList<>(travelRepository
                .findByEmpIdAndStatusAndPayrollProcessed(employeeId, "ADMIN_APPROVED", false));
        for (TravelRequest travel : travels) {
            travel.setPayrollProcessed(true);
            travel.setPayrollReferenceMonth(savedPayroll.getMonth());
            travel.setPayrollProcessedAt(LocalDateTime.now());
        }
        if (!travels.isEmpty()) {
            travelRepository.saveAll(travels);
        }

        auditTrailService.record(
                finalizedBy,
                "PAYROLL_FINALIZED",
                "/payroll/finalize/" + employeeId,
                "SUCCESS",
                "PAYROLL",
                savedPayroll.getId(),
                "month=" + savedPayroll.getMonth() + ", approvedAdditions=" + savedPayroll.getApprovedAdditions() + ", netSalary=" + savedPayroll.getNetSalary()
        );

        return savedPayroll;
    }

    @Transactional
    public List<Payroll> finalizePayrollForMonth(YearMonth payrollMonth, String finalizedBy) {
        List<Payroll> finalizedPayrolls = new ArrayList<>();
        for (Employee employee : employeeRepository.findByStatus("Active")) {
            finalizedPayrolls.add(finalizePayroll(employee.getEmpId(), payrollMonth, finalizedBy));
        }
        return finalizedPayrolls;
    }

    private double safePercentage(Double value) {
        return value == null ? 0.0 : value;
    }

    private double nullSafe(Double value) {
        return value == null ? 0.0 : value;
    }
    
    private double calculateNewRegimeTax(double taxableIncome) {

        if (taxableIncome <= 1200000) {
            return 0.0;
        }

        double tax;

        if (taxableIncome <= 1600000) {
            tax = 60000
                    + (taxableIncome - 1200000) * 0.15;

        } else if (taxableIncome <= 2000000) {
            tax = 120000
                    + (taxableIncome - 1600000) * 0.20;

        } else if (taxableIncome <= 2400000) {
            tax = 200000
                    + (taxableIncome - 2000000) * 0.25;

        } else {
            tax = 300000
                    + (taxableIncome - 2400000) * 0.30;
        }

        return round(tax * 1.04);
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}