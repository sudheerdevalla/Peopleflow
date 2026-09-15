package com.hr.hrapp.service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.EmployeeAttendance;
import com.hr.hrapp.repository.EmployeeAttendanceRepository;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.dto.EmployeePayslip;

@Service
public class EmployeeSalaryService {

    @Autowired
    private EmployeeRepository repo;

    @Autowired
    private EmployeeAttendanceRepository attendanceRepo;

    @Autowired
    private EmployeePayslipPdfService pdfService;

    // ================= PDF =================
    public byte[] generatePayslipPdf(Long id, int leaves) {

        Employee emp = getEmployeeById(id);
        EmployeeAttendance att = attendanceRepo.findByEmployeeId(id);
        EmployeePayslip payslip = calculateSalary(emp, leaves, att.getTotalDays());

        return pdfService.generatePayslipPdf(payslip, id);
    }

    // ================= GET EMPLOYEE =================
    public Employee getEmployeeById(Long id) {
        return repo.findById(id).orElse(null);
    }

    // ================= SIMPLE SALARY =================
 // ================= SPREADSHEET SALARY CALCULATION =================
    public EmployeePayslip calculateSalary(Employee emp, int leaves, int totalDays) {

        // =========================
        // RATES FROM PAYROLL SHEET
        // =========================
        final double BASIC_PCT = 0.50;
        final double HRA_PCT = 0.40;
        final double CONVEYANCE_PCT = 0.12;
        final double TELEPHONE_PCT = 0.08;
        final double INTERNET_PCT = 0.09;
        final double TRAVEL_PCT = 0.10;

        final double CONVEYANCE_CAP = 1600.00;
        final double TELEPHONE_CAP = 1000.00;
        final double INTERNET_CAP = 1200.00;
        final double TRAVEL_CAP = 1250.00;

        final double PF_CEILING = 15000.00;
        final double PF_EMPLOYEE_RATE = 0.12;
        final double PF_EMPLOYER_RATE = 0.12;
        final double EPS_RATE = 0.0833;
        final double PF_ADMIN_RATE = 0.005;
        final double EDLI_RATE = 0.005;

        final double ESI_EMPLOYEE_RATE = 0.0075;
        final double ESI_EMPLOYER_RATE = 0.0325;

        final int DAYS_IN_MONTH = totalDays;

        // =========================
        // MONTHLY GROSS
        // =========================
        double monthlyGross = emp.getMonthlyGrossSalary() != null
                ? emp.getMonthlyGrossSalary().doubleValue()
                : 0.0;

        if (monthlyGross <= 0) {
            throw new IllegalArgumentException(
                    "Monthly Gross Salary is not configured for employee: "
                    + emp.getEmployeeCode());
        }

        // =========================
        // PAID DAYS / ATTENDANCE
        // =========================
        int paidDays = DAYS_IN_MONTH - leaves;

        if (paidDays < 0) {
            paidDays = 0;
        }

        if (paidDays > DAYS_IN_MONTH) {
            paidDays = DAYS_IN_MONTH;
        }

        double attendanceFactor =
                (double) paidDays / DAYS_IN_MONTH;

        // =========================
        // FULL MONTH COMPONENTS
        // =========================
        double fullBasic =
                Math.round(monthlyGross * BASIC_PCT);

        double fullHra =
                Math.round(fullBasic * HRA_PCT);

        double fullConveyance =
                Math.round(fullBasic * CONVEYANCE_PCT);

        double fullTelephone =
                Math.round(fullBasic * TELEPHONE_PCT);

        double fullInternet =
                Math.round(fullBasic * INTERNET_PCT);

        double fullTravel =
                Math.round(fullBasic * TRAVEL_PCT);

        // =========================
        // ATTENDANCE BASED EARNINGS
        // =========================
        double basic =
                Math.round(fullBasic * attendanceFactor);

        double hra =
                Math.round(fullHra * attendanceFactor);

        double conveyance =
                Math.min(
                        CONVEYANCE_CAP,
                        Math.round(fullConveyance * attendanceFactor));

        double telephone =
                Math.min(
                        TELEPHONE_CAP,
                        Math.round(fullTelephone * attendanceFactor));

        double internet =
                Math.min(
                        INTERNET_CAP,
                        Math.round(fullInternet * attendanceFactor));

        double travel =
                Math.min(
                        TRAVEL_CAP,
                        Math.round(fullTravel * attendanceFactor));

        // =========================
        // SPECIAL ALLOWANCE
        // =========================
        double proratedGross =
                Math.round(monthlyGross * attendanceFactor);

        double specialAllowance =
                proratedGross
                - basic
                - hra
                - conveyance
                - telephone
                - internet
                - travel;

        // =========================
        // GROSS EARNING
        // =========================
        double grossEarning =
                basic
                + hra
                + conveyance
                + telephone
                + internet
                + travel
                + specialAllowance;

        // =========================
        // PF
        // =========================
        double pfWages =
                Math.min(basic, PF_CEILING);

        double employeePf =
                Math.round(pfWages * PF_EMPLOYEE_RATE);

        // =========================
        // ESI
        // =========================
        double esiWages =
                emp.isEsiApplicable()
                ? basic
                : 0.0;

        double employeeEsi =
                Math.round(esiWages * ESI_EMPLOYEE_RATE);

        // =========================
        // PROFESSIONAL TAX
        // =========================
        double professionalTax;

        if (grossEarning <= 15000) {
            professionalTax = 0;
        } else if (grossEarning <= 20000) {
            professionalTax = 150;
        } else {
            professionalTax = 200;
        }

        // =========================
        // MANUAL DEDUCTIONS
        // =========================
        double tds = 0.0;
        double groupHealthInsurance = 0.0;
        double advanceSalaryRecovery = 0.0;

        // =========================
        // TOTAL DEDUCTIONS
        // =========================
        double totalDeductions =
                employeePf
                + employeeEsi
                + professionalTax
                + tds
                + groupHealthInsurance
                + advanceSalaryRecovery;

        // =========================
        // NET PAY
        // =========================
        double netPay =
                grossEarning - totalDeductions;

        // =========================
        // EMPLOYER CONTRIBUTIONS
        // =========================
        double employerPf =
                Math.round(pfWages * PF_EMPLOYER_RATE);

        double employerEps =
        		emp.isEpsApplicable()
                        ? Math.round(pfWages * EPS_RATE)
                        : 0.0;

        double employerPfTotal =
                employerPf - employerEps;

        double employerEsi =
                Math.round(esiWages * ESI_EMPLOYER_RATE);

        double pfAdmin =
                Math.round(pfWages * PF_ADMIN_RATE);

        double edli =
                Math.round(pfWages * EDLI_RATE);

        double totalEmployerContribution =
                employerPfTotal
                + employerEsi
                + pfAdmin
                + edli;

        // =========================
        // CTC
        // =========================
        double ctc =
                grossEarning
                + employerPfTotal
                + employerEsi;

        // =========================
        // DTO
        // =========================
        EmployeePayslip payslip = new EmployeePayslip();

        payslip.setId(
                emp.getEmpId() != null
                        ? emp.getEmpId().intValue()
                        : 0);

        payslip.setName(emp.getName());

        payslip.setBasicSalary(basic);
        payslip.setHra(hra);
        payslip.setPf(employeePf);
        payslip.setLeaveDeduction(0.0);

        payslip.setConveyance(conveyance);
        payslip.setTelephone(telephone);
        payslip.setInternet(internet);
        payslip.setTravelAllowance(travel);
        payslip.setSpecialAllowance(specialAllowance);

        payslip.setGrossEarning(grossEarning);

        payslip.setEmployeeEsi(employeeEsi);
        payslip.setProfessionalTax(professionalTax);
        payslip.setTds(tds);
        payslip.setGroupHealthInsurance(groupHealthInsurance);
        payslip.setAdvanceSalaryRecovery(advanceSalaryRecovery);

        payslip.setTotalDeductions(totalDeductions);

        payslip.setEmployerPf(employerPfTotal);
        payslip.setEmployerEps(employerEps);
        payslip.setEmployerEsi(employerEsi);
        payslip.setEmployerPfAdmin(pfAdmin);
        payslip.setEdli(edli);

        payslip.setTotalEmployerContribution(
                totalEmployerContribution);

        payslip.setCtc(ctc);

        payslip.setNetSalary(netPay);

        // =========================
        // EMPLOYEE DETAILS
        // =========================
        payslip.setRole(emp.getRole());
        payslip.setLocation(emp.getLocation());
        payslip.setBankName(emp.getBankName());
        payslip.setAccountNumber(emp.getAccountNumber());
        payslip.setIfsc(emp.getIfsc());

        return payslip;
    }

    // ================= CRUD =================
    public Employee saveEmployee(Employee emp) {
        return repo.save(emp);
    }

    public List<Employee> getAllEmployees() {
        return repo.findAll();
    }

    // NEW: Paginated version to avoid full table scans in production
    public Page<Employee> getAllEmployeesPaginated(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return repo.findAll(pageable);
    }

    public void deleteEmployee(Long id) {
        repo.deleteById(id);
    }

    public Employee updateEmployee(Long id, Employee emp) {

        Employee existing = repo.findById(id).orElse(null);

        if (existing != null) {
            existing.setName(emp.getName());
            existing.setBasicSalary(emp.getBasicSalary());
            existing.setDepartment(emp.getDepartment());
            existing.setEmail(emp.getEmail());
        }

        return repo.save(existing);
    }

    // ================= ADVANCED PAYSLIP =================
    public EmployeePayslip generatePayslip(Long id) {

        Employee emp = repo.findById(id).orElse(null);

        if (emp == null) {
            throw new RuntimeException("Employee not found");
        }

        EmployeeAttendance att = attendanceRepo.findByEmployeeId(id);

        if (att == null) {
            throw new RuntimeException("Attendance not found for employee id: " + id);
        }

        int totalDays = att.getTotalDays();
        int presentDays = att.getPresentDays();
        int leaves = totalDays - presentDays;

        return calculateSalary(emp, leaves, totalDays);
    }
    public String calculateExperience(LocalDate joiningDate) {

        if (joiningDate == null) return "0 Years";

        Period p = Period.between(joiningDate, LocalDate.now());

        return p.getYears() + " Years " + p.getMonths() + " Months";
    }
}