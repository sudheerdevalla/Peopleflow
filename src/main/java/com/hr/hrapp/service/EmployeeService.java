package com.hr.hrapp.service;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.repository.EmployeeRepository;

@Service
public class EmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;

    // ✅ Experience Calculate
    public String calculateExperience(LocalDate joiningDate) {

        if (joiningDate == null)
            return "0 Years";

        Period p =
                Period.between(joiningDate,
                        LocalDate.now());

        return p.getYears() + " Years "
                + p.getMonths() + " Months";
    }

    // ✅ Financial Details Save
    public void updateFinancialDetails(
            String email,
            Employee updated) {

        Employee existing =
                employeeRepository.findByEmail(email);

        existing.setBankName(
                updated.getBankName());

        existing.setAccountNumber(
                updated.getAccountNumber());

        existing.setIfsc(
                updated.getIfsc());

        existing.setPanNumber(
                updated.getPanNumber());


        employeeRepository.save(existing);
    }
}