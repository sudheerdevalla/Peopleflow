package com.hr.hrapp.service;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.Timesheet;
import com.hr.hrapp.payroll.entity.Payroll;
import com.hr.hrapp.payroll.repository.PayrollRepository;
import com.hr.hrapp.repository.TimesheetRepository;
import com.hr.hrapp.util.PayrollMonthUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class TimesheetEntryService {

    private static final String FINALIZED = "FINALIZED";

    @Autowired
    private TimesheetRepository timesheetRepository;

    @Autowired
    private LocationService locationService;

    @Autowired
    private TimesheetValidationService timesheetValidationService;

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private AuditTrailService auditTrailService;

    @Transactional
    public Timesheet saveOrUpdate(Employee employee,
                                  LocalDate date,
                                  String location,
                                  Integer hours,
                                  Integer training,
                                  String clientName,
                                  String projectName,
                                  String workDescription,
                                  Double latitude,
                                  Double longitude,
                                  String actorUsername) {
    	
    	System.out.println("========== TIMESHEET SAVE START ==========");
    	System.out.println("Employee ID = " + employee.getEmpId());
    	System.out.println("Date = " + date);
    	System.out.println("Location = " + location);
    	System.out.println("Hours = " + hours);
    	System.out.println("Training = " + training);
    	System.out.println("Latitude = " + latitude);
    	System.out.println("Longitude = " + longitude);
    	
        validateEditableMonth(employee.getEmpId(), date);
        validateHours(hours, training);

        Optional<Timesheet> existingOpt =
                timesheetRepository.findByEmployeeIdAndDate(
                        employee.getEmpId(), date);
        
        System.out.println("Existing timesheet present = " + existingOpt.isPresent());

        if (existingOpt.isPresent()) {

            Timesheet existing = existingOpt.get();

            String status = existing.getStatus();

            if ("SUBMITTED".equalsIgnoreCase(status)
                    || "APPROVED".equalsIgnoreCase(status)
                    || "FINALIZED".equalsIgnoreCase(status)) {

                throw new IllegalStateException(
                        "This timesheet has already been submitted and cannot be edited."
                );
            }
        }

        Timesheet target = existingOpt.orElseGet(Timesheet::new);

        target.setEmployeeId(employee.getEmpId());
        target.setDate(date);
        target.setHours(hours == null ? 0 : hours);
        target.setTraining(training == null ? 0 : training);
        target.setWorkLocation(location);
        target.setClientName(clientName);
        target.setProjectName(projectName);
        target.setWorkDescription(workDescription);
        target.setLatitude(latitude);
        target.setLongitude(longitude);
        target.setExpectedLocation(employee.getLocation());
        target.setActualLocation(resolveActualLocation(location, latitude, longitude));
        if (target.getCreatedAt() == null) {
            target.setCreatedAt(LocalDateTime.now());
        }
        target.setReviewedAt(null);
        target.setReviewedBy(null);
        
     // Default status for a submitted timesheet
        target.setStatus("SUBMITTED");


        System.out.println("Before validation. Status = " + target.getStatus());

        timesheetValidationService.validateAndNotify(target, employee);

        System.out.println("After validation. Status = " + target.getStatus());

        target.setStatus(
                target.getStatus() == null
                        ? "SUBMITTED"
                        : target.getStatus()
        );

        System.out.println("Before DB save. Status = " + target.getStatus());

        Timesheet saved = timesheetRepository.save(target);

        System.out.println("AFTER DB SAVE. ID = " + saved.getId());
        System.out.println("AFTER DB SAVE. Status = " + saved.getStatus());
        System.out.println("========== TIMESHEET SAVE END ==========");

        auditTrailService.record(
                actorUsername,
                existingOpt.isPresent() ? "TIMESHEET_UPDATED" : "TIMESHEET_CREATED",
                "/user/timesheet",
                "SUCCESS",
                "TIMESHEET",
                saved.getId(),
                "date=" + date + ", status=" + saved.getStatus() + ", hours=" + saved.getHours() + ", training=" + saved.getTraining()
        );

        return saved;
    }

    @Transactional
    public Timesheet managerReviewLocationException(Long timesheetId,
                                                    boolean approved,
                                                    String reviewerUsername) {
        Timesheet timesheet = timesheetRepository.findById(timesheetId)
                .orElseThrow(() -> new IllegalArgumentException("Timesheet not found"));

        validateEditableMonth(timesheet.getEmployeeId(), timesheet.getDate());

        if (timesheet.getStatus() == null || !timesheet.getStatus().equalsIgnoreCase("LOCATION_MISMATCH")) {
            throw new IllegalStateException("Only location mismatch timesheets can be reviewed through this action");
        }

        timesheet.setStatus(approved ? "APPROVED" : "REJECTED");
        timesheet.setReviewedAt(LocalDateTime.now());
        timesheet.setReviewedBy(reviewerUsername);

        Timesheet saved = timesheetRepository.save(timesheet);
        auditTrailService.record(
                reviewerUsername,
                approved ? "TIMESHEET_APPROVED" : "TIMESHEET_REJECTED",
                approved ? "/admin/manager/approve-location" : "/admin/manager/reject-location",
                "SUCCESS",
                "TIMESHEET",
                saved.getId(),
                "date=" + saved.getDate() + ", employeeId=" + saved.getEmployeeId() + ", finalStatus=" + saved.getStatus()
        );
        return saved;
    }

    public void validateEditableMonth(Long employeeId, LocalDate date) {

        String month = PayrollMonthUtil.format(date);

        System.out.println("DEBUG Payroll Month = " + month);

        Payroll finalizedPayroll = payrollRepository
                .findByEmployeeIdAndMonth(employeeId, month)
                .filter(p -> FINALIZED.equalsIgnoreCase(p.getStatus()))
                .orElse(null);

        System.out.println("DEBUG Finalized Payroll = " + finalizedPayroll);

        if (finalizedPayroll != null) {

            throw new IllegalStateException(
                    "Timesheet is locked because payroll for "
                    + month + " is already finalized"
            );
        }
    }

    private void validateHours(Integer hours, Integer training) {
        int workHours = hours == null ? 0 : hours;
        int trainingHours = training == null ? 0 : training;

        if (workHours < 0 || trainingHours < 0) {
            throw new IllegalArgumentException("Hours and training must be zero or positive");
        }
        if (workHours > 24 || trainingHours > 24) {
            throw new IllegalArgumentException("Hours and training cannot exceed 24 for a single day");
        }
        if (workHours + trainingHours > 24) {
            throw new IllegalArgumentException("Combined work and training hours cannot exceed 24 in a day");
        }
    }

    private String resolveActualLocation(String fallbackLocation, Double latitude, Double longitude) {
        if (latitude == null || longitude == null) {
            return fallbackLocation;
        }
        return locationService.getCity(latitude, longitude);
    }
}
