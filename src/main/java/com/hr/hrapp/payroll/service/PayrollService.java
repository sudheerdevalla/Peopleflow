package com.hr.hrapp.payroll.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.TravelRequest;
import com.hr.hrapp.payroll.entity.Payroll;
import com.hr.hrapp.payroll.repository.PayrollRepository;
import com.hr.hrapp.repository.TravelRequestRepository;

@Service
public class PayrollService {

    @Autowired
    private PayrollRepository payrollRepository;
    
    @Autowired
    private TravelRequestRepository travelRepository;

    public Payroll calculateSalary(Employee employee) {

        // =========================
        // BASIC SALARY
        // =========================

        double basicSalary =
                employee.getBasicSalary();

        // =========================
        // HRA
        // =========================

        double hra =
                basicSalary
                * (employee.getHraPercentage() != null ? employee.getHraPercentage() : 0.0)
                / 100;

        // =========================
        // BONUS
        // =========================

        double bonus =
                basicSalary
                * (employee.getBonusPercentage() != null ? employee.getBonusPercentage() : 0.0)
                / 100;
        
        double travelAllowance =
                travelRepository
                .getApprovedTravelAllowance(
                        employee.getEmpId());
        
        if(employee.getTravelAllowance() != null){

            travelAllowance =
                    travelAllowance
                    + employee.getTravelAllowance();
        }
        System.out.println(
        	    "FINAL TRAVEL ALLOWANCE = "
        	    + travelAllowance);
        
        

        // =========================
        // PF
        // =========================

        double pf =
                basicSalary * 0.12;

        // =========================
        // TAX
        // =========================

        double tax =
                basicSalary * 0.05;

        // =========================
        // TOTAL DEDUCTIONS
        // =========================

        double deductions =
                pf + tax;

        // =========================
        // NET SALARY
        // =========================

        double netSalary =
                (basicSalary
                + hra
                + bonus
                + travelAllowance)
                - deductions;

        // =========================
        // MONTH
        // =========================

        LocalDate now =
                LocalDate.now();

        String currentMonth =
                now.getMonth()
                + " "
                + now.getYear();

        // =========================
        // CHECK EXISTING PAYROLL
        // =========================

        Payroll payroll =
                payrollRepository
                .findByEmployeeIdAndMonth(
                        employee.getEmpId(),
                        currentMonth)
                .orElse(new Payroll());

        // =========================
        // SET DATA
        // =========================

        payroll.setEmployeeId(
                employee.getEmpId());

        payroll.setEmployeeName(
                employee.getName());

        payroll.setBasicSalary(
                basicSalary);

        payroll.setHra(
                hra);

        payroll.setBonus(
                bonus);
        
        payroll.setTravelAllowance(
                travelAllowance);

        payroll.setPf(
                pf);

        payroll.setTax(
                tax);

        payroll.setDeductions(
                deductions);

        payroll.setNetSalary(
                netSalary);

        payroll.setMonth(
                currentMonth);
        
        
     // =========================
     // MARK TRAVELS AS PROCESSED
     // =========================

     List<TravelRequest> travels =
             travelRepository
             .findByEmpIdAndStatusAndPayrollProcessed(
                     employee.getEmpId(),
                     "ADMIN_APPROVED",
                     false);

     for(TravelRequest t : travels){

         t.setPayrollProcessed(true);
     }

     travelRepository.saveAll(travels);

        // =========================
        // SAVE
        // =========================

        return payrollRepository
                .save(payroll);
    }
}