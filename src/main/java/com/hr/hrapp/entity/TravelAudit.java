package com.hr.hrapp.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class TravelAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long travelRequestId;

    private Long performedByEmployeeId;

    private String action; // REQUESTED, MANAGER_APPROVED, ADMIN_APPROVED, REJECTED, COMPLETED

    private String comments;

    private LocalDateTime performedAt = LocalDateTime.now();

    public Long getId() { return id; }
    public Long getTravelRequestId() { return travelRequestId; }
    public void setTravelRequestId(Long travelRequestId) { this.travelRequestId = travelRequestId; }
    public Long getPerformedByEmployeeId() { return performedByEmployeeId; }
    public void setPerformedByEmployeeId(Long performedByEmployeeId) { this.performedByEmployeeId = performedByEmployeeId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getComments() { return comments; }
    public void setComments(String comments) { this.comments = comments; }
    public LocalDateTime getPerformedAt() { return performedAt; }
    public void setPerformedAt(LocalDateTime performedAt) { this.performedAt = performedAt; }
}
