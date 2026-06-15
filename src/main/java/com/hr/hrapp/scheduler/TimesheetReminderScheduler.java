package com.hr.hrapp.scheduler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.Holiday;
import com.hr.hrapp.entity.Notification;
import com.hr.hrapp.entity.TimesheetPenalty;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.repository.HolidayRepository;
import com.hr.hrapp.repository.NotificationRepository;
import com.hr.hrapp.repository.TimesheetPenaltyRepository;
import com.hr.hrapp.repository.TimesheetRepository;
import com.hr.hrapp.service.EmailService;

@Component

public class TimesheetReminderScheduler {
	
	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private TimesheetRepository timesheetRepository;

	@Autowired
	private EmailService emailService;

	@Autowired
	private HolidayRepository holidayRepository;

	@Autowired
	private NotificationRepository notificationRepository;
	
	@Autowired
	private TimesheetPenaltyRepository penaltyRepository;
	
	
	@Scheduled(cron = "0 0 9 * * *")
	public void checkMissingTimesheets() {
		
		System.out.println("Timesheet Scheduler Running...");

	    List<Employee> employees =
	            employeeRepository.findAll();

	    for (Employee emp : employees) {

	        // =========================
	        // DAY 1 REMINDER
	        // =========================

	        LocalDate reminderDate =
	                LocalDate.now().minusDays(1);

	        boolean day1Submitted =
	                timesheetRepository
	                        .existsByEmployeeIdAndDate(
	                                emp.getEmpId(),
	                                reminderDate);

	        Optional<TimesheetPenalty> reminderPenaltyOpt =
	                penaltyRepository
	                        .findByEmployeeIdAndTimesheetDate(
	                                emp.getEmpId(),
	                                reminderDate);

	        TimesheetPenalty reminderPenalty;

	        if (reminderPenaltyOpt.isEmpty()) {

	            reminderPenalty =
	                    new TimesheetPenalty();

	            reminderPenalty.setEmployeeId(
	                    emp.getEmpId());

	            reminderPenalty.setTimesheetDate(
	                    reminderDate);

	            reminderPenalty.setReminderSent(false);
	            reminderPenalty.setWarningSent(false);
	            reminderPenalty.setLeaveDeducted(false);

	            penaltyRepository.save(
	                    reminderPenalty);

	        } else {

	            reminderPenalty =
	                    reminderPenaltyOpt.get();
	        }

	        if (!day1Submitted &&
	                !reminderPenalty.isReminderSent()) {

	            emailService.sendMail(
	                    emp.getEmail(),
	                    "Timesheet Reminder",
	                    "Please submit your timesheet for "
	                            + reminderDate
	            );

	            reminderPenalty.setReminderSent(true);

	            penaltyRepository.save(
	                    reminderPenalty);
	        }

	        // =========================
	        // DAY 3 WARNING
	        // =========================

	        LocalDate warningDate =
	                LocalDate.now().minusDays(3);

	        boolean day3Submitted =
	                timesheetRepository
	                        .existsByEmployeeIdAndDate(
	                                emp.getEmpId(),
	                                warningDate);

	        Optional<TimesheetPenalty> warningPenaltyOpt =
	                penaltyRepository
	                        .findByEmployeeIdAndTimesheetDate(
	                                emp.getEmpId(),
	                                warningDate);

	        if (warningPenaltyOpt.isPresent()) {

	            TimesheetPenalty warningPenalty =
	                    warningPenaltyOpt.get();

	            if (!day3Submitted &&
	                    !warningPenalty.isWarningSent()) {

	                emailService.sendMail(
	                        emp.getEmail(),
	                        "Timesheet Warning",
	                        "You have not submitted your timesheet for "
	                                + warningDate
	                                + ". Leave will be deducted if not submitted."
	                );

	                warningPenalty.setWarningSent(true);

	                penaltyRepository.save(
	                        warningPenalty);
	            }
	        }

	        // =========================
	        // DAY 5 LEAVE DEDUCTION
	        // =========================

	        LocalDate deductionDate =
	                LocalDate.now().minusDays(5);

	        boolean day5Submitted =
	                timesheetRepository
	                        .existsByEmployeeIdAndDate(
	                                emp.getEmpId(),
	                                deductionDate);

	        Optional<TimesheetPenalty> deductionPenaltyOpt =
	                penaltyRepository
	                        .findByEmployeeIdAndTimesheetDate(
	                                emp.getEmpId(),
	                                deductionDate);

	        if (deductionPenaltyOpt.isPresent()) {

	            TimesheetPenalty deductionPenalty =
	                    deductionPenaltyOpt.get();

	            if (!day5Submitted &&
	                    !deductionPenalty.isLeaveDeducted()) {

	                if (emp.getAnnualLeaves() > 0) {

	                    emp.setAnnualLeaves(
	                            emp.getAnnualLeaves() - 1);

	                    employeeRepository.save(emp);
	                }

	                Notification n =
	                        new Notification();

	                n.setEmployeeId(
	                        emp.getEmpId());

	                n.setMessage(
	                        "1 Annual Leave deducted due to missing timesheet.");

	                n.setRead(false);

	                n.setCreatedAt(
	                        LocalDateTime.now());

	                notificationRepository.save(n);

	                emailService.sendMail(
	                        emp.getEmail(),
	                        "Leave Deducted",
	                        "1 Annual Leave has been deducted because timesheet was not submitted for "
	                                + deductionDate
	                );

	                deductionPenalty.setLeaveDeducted(true);

	                penaltyRepository.save(
	                        deductionPenalty);
	            }
	        }
	    }
	}

}
