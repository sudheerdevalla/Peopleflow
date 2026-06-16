package com.hr.hrapp.payroll.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.payroll.entity.Payroll;
import com.hr.hrapp.payroll.repository.PayrollRepository;
import com.hr.hrapp.repository.TravelRequestRepository;

@ExtendWith(MockitoExtension.class)
class PayrollServiceTest {

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private TravelRequestRepository travelRepository;

    @InjectMocks
    private PayrollService payrollService;

    @Test
    void testCalculateSalary() {

        Employee employee = new Employee();

        employee.setEmpId(1L);
        employee.setName("Sudheer");
        employee.setBasicSalary(30000);
        employee.setHraPercentage(15.0);
        employee.setBonusPercentage(10.0);
        employee.setTravelAllowance(1000.0);

        when(travelRepository.getApprovedTravelAllowance(1L))
                .thenReturn(2000.0);

        when(payrollRepository.findByEmployeeIdAndMonth(
                any(),
                any()))
                .thenReturn(Optional.empty());

        when(travelRepository.findByEmpIdAndStatusAndPayrollProcessed(
                1L,
                "ADMIN_APPROVED",
                false))
                .thenReturn(new ArrayList<>());

        when(payrollRepository.save(any(Payroll.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Payroll payroll =
                payrollService.calculateSalary(employee);

        assertEquals(4500.0, payroll.getHra());
        assertEquals(3000.0, payroll.getBonus());
        assertEquals(3000.0, payroll.getTravelAllowance());
        assertEquals(35400.0, payroll.getNetSalary());
    }
}
