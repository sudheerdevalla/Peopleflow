package com.hr.hrapp.payroll.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hr.hrapp.entity.Employee;

class PayrollCalculationTest {

    @Test
    void testSalaryCalculation() {

        Employee emp = new Employee();

        emp.setBasicSalary(50000);
        emp.setHraPercentage(20.0);
        emp.setBonusPercentage(10.0);
        emp.setTravelAllowance(3000.0);

        double hra = emp.getBasicSalary() * 20 / 100;
        double bonus = emp.getBasicSalary() * 10 / 100;

        double gross =
                emp.getBasicSalary()
                + hra
                + bonus
                + emp.getTravelAllowance();

        assertEquals(68000, gross);
    }
}