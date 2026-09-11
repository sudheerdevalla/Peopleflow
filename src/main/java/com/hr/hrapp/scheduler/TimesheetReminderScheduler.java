package com.hr.hrapp.scheduler;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	private static final Logger logger = LoggerFactory.getLogger(TimesheetReminderScheduler.class);
	
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

	    logger.info("Timesheet Scheduler Running...");

	    List<Employee> employees = employeeRepository.findAll();

	    for (Employee emp : employees) {

	        LocalDate today = LocalDate.now();

	        /*
	         * Find the 5th and 7th working days counting backwards
	         * from today. Weekends and configured holidays are excluded.
	         */
	        LocalDate fifthWorkingDay = getPreviousWorkingDay(today, 5);
	        LocalDate seventhWorkingDay = getPreviousWorkingDay(today, 7);

	        // =====================================================
	        // DAY 5 - ONE TIME REMINDER
	        // =====================================================

	        if (fifthWorkingDay != null) {

	            boolean submitted = timesheetRepository
	                    .existsByEmployeeIdAndDate(
	                            emp.getEmpId(),
	                            fifthWorkingDay);

	            TimesheetPenalty penalty =
	                    getOrCreatePenalty(emp.getEmpId(), fifthWorkingDay);

	            if (!submitted && !penalty.isReminderSent()) {

	                String subject = "Timesheet Submission Reminder";

	                String message =
	                        "Dear " + emp.getFirstName() + ",\n\n"
	                        + "Our records indicate that your timesheet has not been "
	                        + "submitted for the required 5 working days.\n\n"
	                        + "Please submit your pending timesheet as soon as possible.\n\n"
	                        + "Important: If the timesheet remains unsubmitted until "
	                        + "the 7th working day, 1 Annual Leave will be deducted "
	                        + "automatically in accordance with the timesheet policy.\n\n"
	                        + "Please complete your timesheet to avoid leave deduction.\n\n"
	                        + "Regards,\n"
	                        + "HR Team\n"
	                        + "PeopleFlow";

	                emailService.sendMail(
	                        emp.getEmail(),
	                        subject,
	                        message
	                );

	                penalty.setReminderSent(true);
	                penaltyRepository.save(penalty);
	            }
	        }

	        // =====================================================
	        // DAY 7 - LEAVE DEDUCTION
	        // =====================================================

	        if (seventhWorkingDay != null) {

	            boolean submitted = timesheetRepository
	                    .existsByEmployeeIdAndDate(
	                            emp.getEmpId(),
	                            seventhWorkingDay);

	            TimesheetPenalty penalty =
	                    getOrCreatePenalty(emp.getEmpId(), seventhWorkingDay);

	            if (!submitted && !penalty.isLeaveDeducted()) {

	                if (emp.getAnnualLeaves() > 0) {

	                    emp.setAnnualLeaves(emp.getAnnualLeaves() - 1);
	                    employeeRepository.save(emp);

	                    Notification n = new Notification();
	                    n.setEmployeeId(emp.getEmpId());
	                    n.setMessage(
	                            "1 Annual Leave deducted due to missing timesheet."
	                    );
	                    n.setRead(false);
	                    n.setCreatedAt(LocalDateTime.now());

	                    notificationRepository.save(n);

	                    emailService.sendMail(
	                            emp.getEmail(),
	                            "Leave Deducted",
	                            "1 Annual Leave has been deducted because your "
	                            + "timesheet was not submitted for "
	                            + seventhWorkingDay
	                    );
	                }

	                penalty.setLeaveDeducted(true);
	                penaltyRepository.save(penalty);
	            }
	        }
	    }
	}

	// Create or fetch a TimesheetPenalty for a given employee/date
	private TimesheetPenalty getOrCreatePenalty(Long empId, LocalDate date) {
		Optional<TimesheetPenalty> opt = penaltyRepository.findByEmployeeIdAndTimesheetDate(empId, date);

		if (opt.isPresent()) return opt.get();

		TimesheetPenalty p = new TimesheetPenalty();
		p.setEmployeeId(empId);
		p.setTimesheetDate(date);
		p.setReminderSent(false);
		p.setWarningSent(false);
		p.setLeaveDeducted(false);
		return penaltyRepository.save(p);
	}

	// Check if the date is a working day (not weekend and not a holiday)
	private boolean isWorkday(LocalDate date) {
		if (date == null) return false;
		DayOfWeek dow = date.getDayOfWeek();
		if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) return false;
		Holiday h = holidayRepository.findByHolidayDate(date);
		return h == null;
	}
	private LocalDate getPreviousWorkingDay(LocalDate fromDate, int workingDaysBack) {

	    LocalDate date = fromDate;
	    int count = 0;

	    while (count < workingDaysBack) {

	        date = date.minusDays(1);

	        if (isWorkday(date)) {
	            count++;
	        }
	    }

	    return date;
	}

}