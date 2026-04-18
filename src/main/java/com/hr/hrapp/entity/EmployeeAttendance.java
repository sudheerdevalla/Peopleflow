package com.hr.hrapp.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "attendance")
public class EmployeeAttendance {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;

	private Long employeeId;
	private int totalDays;
	private int presentDays;

	// getters & setters

	public int getId() {
		return id;
	}

	public Long getEmployeeId() {
		return employeeId;
	}

	public void setEmployeeId(Long employeeId) {
		this.employeeId = employeeId;
	}

	public int getTotalDays() {
		return totalDays;
	}

	public void setTotalDays(int totalDays) {
		this.totalDays = totalDays;
	}

	public int getPresentDays() {
		return presentDays;
	}

	public void setPresentDays(int presentDays) {
		this.presentDays = presentDays;
	}
}