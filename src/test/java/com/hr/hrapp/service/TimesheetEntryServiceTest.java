package com.hr.hrapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.Timesheet;
import com.hr.hrapp.payroll.repository.PayrollRepository;
import com.hr.hrapp.repository.TimesheetRepository;

@ExtendWith(MockitoExtension.class)
class TimesheetEntryServiceTest {

    @Mock
    private TimesheetRepository timesheetRepository;

    @Mock
    private LocationService locationService;

    @Mock
    private TimesheetValidationService timesheetValidationService;

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private AuditTrailService auditTrailService;

    @InjectMocks
    private TimesheetEntryService timesheetEntryService;

    @Test
    void shouldRejectCombinedHoursAbove24() {
        Employee employee = new Employee();
        employee.setEmpId(1L);
        employee.setLocation("Hyderabad");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> timesheetEntryService.saveOrUpdate(
                        employee,
                        LocalDate.of(2026, 9, 1),
                        "Hyderabad",
                        20,
                        5,
                        "Client",
                        "Project",
                        "Work",
                        null,
                        null,
                        "user@example.com"
                ));

        assertEquals("Combined work and training hours cannot exceed 24 in a day", ex.getMessage());
        verify(timesheetRepository, never()).save(any());
    }

    @Test
    void shouldSaveValidatedTimesheet() {
        Employee employee = new Employee();
        employee.setEmpId(1L);
        employee.setLocation("Hyderabad");

        when(timesheetRepository.findByEmployeeIdAndDate(1L, LocalDate.of(2026, 9, 1)))
                .thenReturn(Optional.empty());
        when(timesheetRepository.save(any(Timesheet.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Timesheet saved = timesheetEntryService.saveOrUpdate(
                employee,
                LocalDate.of(2026, 9, 1),
                "Hyderabad",
                8,
                1,
                "Client",
                "Project",
                "Work",
                null,
                null,
                "user@example.com"
        );

        assertEquals(8, saved.getHours());
        assertEquals(1, saved.getTraining());
        verify(timesheetValidationService).validateAndNotify(any(Timesheet.class), any(Employee.class));
        verify(auditTrailService).record(any(), any(), any(), any(), any(), any(), any());
    }
}
