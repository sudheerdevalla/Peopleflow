package com.hr.hrapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hr.hrapp.entity.TravelRequest;

public interface TravelRequestRepository
        extends JpaRepository<TravelRequest, Long> {

    List<TravelRequest> findByEmpId(Long empId);

    List<TravelRequest> findByStatus(String status);
    
    // Find overlapping requests for an employee (excluding a specific id if provided)
    @org.springframework.data.jpa.repository.Query("SELECT t FROM TravelRequest t WHERE t.empId = :empId AND t.id <> COALESCE(:excludeId, -1) AND NOT (t.toDate < :fromDate OR t.fromDate > :toDate)")
    List<TravelRequest> findOverlappingRequests(@org.springframework.data.repository.query.Param("empId") Long empId,
                                                @org.springframework.data.repository.query.Param("fromDate") java.time.LocalDate fromDate,
                                                @org.springframework.data.repository.query.Param("toDate") java.time.LocalDate toDate,
                                                @org.springframework.data.repository.query.Param("excludeId") Long excludeId);
    
    @Query("""
    		SELECT COALESCE(SUM(t.travelAllowance),0)
    		FROM TravelRequest t
    		WHERE t.empId = :empId
    		AND t.status = 'ADMIN_APPROVED'
    		AND t.payrollProcessed = false
    		""")
    		Double getApprovedTravelAllowance(
    		        @org.springframework.data.repository.query.Param("empId")
    		        Long empId);
    
    List<TravelRequest>
    findByEmpIdAndStatusAndPayrollProcessed(
            Long empId,
            String status,
            boolean payrollProcessed);
    
}