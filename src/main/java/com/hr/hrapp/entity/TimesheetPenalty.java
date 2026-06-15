package com.hr.hrapp.entity;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class TimesheetPenalty {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employeeId;

    private LocalDate timesheetDate;

    private boolean reminderSent;

    private boolean warningSent;

    private boolean leaveDeducted;

    public TimesheetPenalty() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public LocalDate getTimesheetDate() {
        return timesheetDate;
    }

    public void setTimesheetDate(LocalDate timesheetDate) {
        this.timesheetDate = timesheetDate;
    }

    public boolean isReminderSent() {
        return reminderSent;
    }

    public void setReminderSent(boolean reminderSent) {
        this.reminderSent = reminderSent;
    }

    public boolean isWarningSent() {
        return warningSent;
    }

    public void setWarningSent(boolean warningSent) {
        this.warningSent = warningSent;
    }

    public boolean isLeaveDeducted() {
        return leaveDeducted;
    }

    public void setLeaveDeducted(boolean leaveDeducted) {
        this.leaveDeducted = leaveDeducted;
    }
}