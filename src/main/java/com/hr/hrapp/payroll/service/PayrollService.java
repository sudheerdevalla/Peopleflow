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
                .filter(t -> t.getStatus() != null
                        && t.getStatus().equalsIgnoreCase("APPROVED"))
                .count();

        int payableDays = approvedTimesheetDays > 0 ? approvedTimesheetDays : workingDays;

        double monthlyGross = employee.getMonthlyGrossSalary() != null
                ? employee.getMonthlyGrossSalary().doubleValue()
                : 0.0;

        if (monthlyGross <= 0) {
            throw new IllegalArgumentException(
                    "Monthly Gross Salary is not configured for employee: "
                    + employee.getEmployeeCode());
        }
        
        double fullBasic = round(monthlyGross * 0.50);
        
        double fullHra = round(fullBasic * 0.40);
        double fullConveyance = round(fullBasic * 0.12);
        double fullTelephone = round(fullBasic * 0.08);
        double fullInternet = round(fullBasic * 0.09);
        double fullTravel = round(fullBasic * 0.10);
        
        double attendanceFactor =
                (double) payableDays / Math.max(workingDays, 1);

        double basic =
                round(fullBasic * attendanceFactor);

        double hra =
                round(fullHra * attendanceFactor);

        double conveyance =
                Math.min(1600.00, round(fullConveyance * attendanceFactor));

        double telephone =
                Math.min(1000.00, round(fullTelephone * attendanceFactor));

        double internet =
                Math.min(1200.00, round(fullInternet * attendanceFactor));

        double travel =
                Math.min(1250.00, round(fullTravel * attendanceFactor));
        
        double proratedGross =
                round(monthlyGross * attendanceFactor);

        double specialAllowance =
                proratedGross
                - basic
                - hra
                - conveyance
                - telephone
                - internet
                - travel;
        double grossEarning =
                round(basic
                        + hra
                        + conveyance
                        + telephone
                        + internet
                        + travel
                        + specialAllowance);
        double pfWages =
                Math.min(basic, 15000.00);

        double pf =
                round(pfWages * 0.12);
        
        double esiWages =
                employee.isEsiApplicable() ? basic : 0.0;

        double employeeEsi =
                round(esiWages * 0.0075);
        
        double professionalTax = 0.0;

        if (grossEarning > 20000) {
            professionalTax = 200.0;
        } else if (grossEarning > 15000) {
            professionalTax = 150.0;
        }
        
        double tds = 0.0;
        double groupHealthInsurance = 0.0;
        double advanceSalaryRecovery = 0.0;
        
        double totalDeductions =
                round(pf
                        + employeeEsi
                        + professionalTax
                        + tds
                        + groupHealthInsurance
                        + advanceSalaryRecovery);

        double netSalary =
                round(grossEarning - totalDeductions);
        
        double employerPf =
                round(pfWages * 0.12);

        double employerEps =
                employee.isEpsApplicable()
                        ? round(pfWages * 0.0833)
                        : 0.0;

        double employerPfTotal =
                employerPf - employerEps;

        double employerEsi =
                round(esiWages * 0.0325);
        
        double pfAdmin =
                round(pfWages * 0.005);

        double edli =
                round(pfWages * 0.005);
        
        double totalEmployerContribution =
                employerPfTotal
                + employerEsi
                + pfAdmin
                + edli;

        double ctc =
                grossEarning
                + employerPfTotal
                + employerEsi;

        

        Payroll payroll = existingPayroll != null ? existingPayroll : new Payroll();
        payroll.setEmployeeId(employee.getEmpId());
        payroll.setEmployeeName(employee.getName());
        payroll.setBasicSalary(basic);

        payroll.setHra(hra);

        payroll.setConveyance(conveyance);

        payroll.setTelephone(telephone);

        payroll.setInternet(internet);

        payroll.setTravelAllowance(travel);

        payroll.setSpecialAllowance(specialAllowance);
        payroll.setApprovedAdditions(0.0);

        payroll.setGrossSalary(grossEarning);

        payroll.setGrossEarning(grossEarning);

        payroll.setPf(pf);

        payroll.setEmployeeEsi(employeeEsi);

        payroll.setProfessionalTax(professionalTax);

        payroll.setTds(tds);

        payroll.setGroupHealthInsurance(groupHealthInsurance);

        payroll.setAdvanceSalaryRecovery(advanceSalaryRecovery);

        payroll.setDeductions(totalDeductions);

        payroll.setTotalDeductions(totalDeductions);

        payroll.setEmployerPf(employerPfTotal);

        payroll.setEmployerEps(employerEps);

        payroll.setEmployerEsi(employerEsi);

        payroll.setEmployerPfAdmin(pfAdmin);

        payroll.setEdli(edli);

        payroll.setTotalEmployerContribution(totalEmployerContribution);

        payroll.setCtc(ctc);

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