package com.hr.hrapp.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hr.hrapp.repository.CandidateRepository;

@Service
public class RecruitmentService {

    @Autowired
    private CandidateRepository candidateRepository;

    // Return total candidates
    public long getTotalCandidates() {
        return candidateRepository.count();
    }

    // Return count for a given status
    public long getCountByStatus(String status) {
        return candidateRepository.countByStatus(status);
    }

    // Compute distribution of candidates by status using a DB GROUP BY query
    // Returns a map where key=status, value=count
    public Map<String, Long> getStatusCounts() {
        List<Object[]> rows = candidateRepository.countCandidatesGroupByStatus();

        Map<String, Long> map = new HashMap<>();

        for (Object[] r : rows) {
            String status = r[0] == null ? "UNKNOWN" : r[0].toString();
            Number n = (Number) r[1];
            long cnt = n == null ? 0L : n.longValue();
            map.put(status, cnt);
        }

        return map;
    }

    // Human-friendly display names for statuses
    public Map<String, String> getStatusDisplayNames() {
        Map<String, String> m = new HashMap<>();
        m.put("APPLIED", "Applied");
        m.put("SHORTLISTED", "Shortlisted");
        m.put("INTERVIEW_SCHEDULED", "Interview Scheduled");
        m.put("SELECTED", "Selected");
        m.put("REJECTED", "Rejected");
        m.put("OFFER_SENT", "Offer Sent");
        m.put("UNKNOWN", "Unknown");
        return m;
    }

}
