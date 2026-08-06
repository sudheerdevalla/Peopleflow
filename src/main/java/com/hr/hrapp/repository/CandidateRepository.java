package com.hr.hrapp.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.hr.hrapp.entity.Candidate;

public interface CandidateRepository
        extends JpaRepository<Candidate, Long> {

    List<Candidate> findByStatus(String status);
    
    long countByStatus(String status);

    // Return counts grouped by status as list of [status, count]
    @Query("SELECT c.status, COUNT(c) FROM Candidate c GROUP BY c.status")
    List<Object[]> countCandidatesGroupByStatus();

}