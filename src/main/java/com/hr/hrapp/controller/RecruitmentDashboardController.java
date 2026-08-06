package com.hr.hrapp.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.hr.hrapp.service.RecruitmentService;

@Controller
@RequestMapping("/recruitment")
public class RecruitmentDashboardController {

    @Autowired
    private RecruitmentService recruitmentService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        long total = recruitmentService.getTotalCandidates();
        long applied = recruitmentService.getCountByStatus("APPLIED");
        long shortlisted = recruitmentService.getCountByStatus("SHORTLISTED");
        long interviewScheduled = recruitmentService.getCountByStatus("INTERVIEW_SCHEDULED");
        long selected = recruitmentService.getCountByStatus("SELECTED");
        long rejected = recruitmentService.getCountByStatus("REJECTED");
        long offerSent = recruitmentService.getCountByStatus("OFFER_SENT");

        model.addAttribute("totalCandidates", total);
        model.addAttribute("appliedCandidates", applied);
        model.addAttribute("shortlistedCandidates", shortlisted);
        model.addAttribute("interviewScheduledCandidates", interviewScheduled);
        model.addAttribute("selectedCandidates", selected);
        model.addAttribute("rejectedCandidates", rejected);
        model.addAttribute("offerSentCandidates", offerSent);

        // initial distribution for server-side render
        Map<String, Long> distribution = recruitmentService.getStatusCounts();
        List<String> labels = distribution.keySet().stream().sorted().collect(Collectors.toList());
        List<Long> values = labels.stream().map(distribution::get).collect(Collectors.toList());

        model.addAttribute("statusLabels", labels);
        model.addAttribute("statusValues", values);

        // status display names map available for server-side if needed
        model.addAttribute("statusDisplayNames", recruitmentService.getStatusDisplayNames());

        return "recruitment-dashboard";
    }

    // JSON endpoint for chart + metric data (used by client-side polling)
    @GetMapping("/data")
    @ResponseBody
    public ResponseEntity<?> data() {

        long total = recruitmentService.getTotalCandidates();
        long applied = recruitmentService.getCountByStatus("APPLIED");
        long shortlisted = recruitmentService.getCountByStatus("SHORTLISTED");
        long interviewScheduled = recruitmentService.getCountByStatus("INTERVIEW_SCHEDULED");
        long selected = recruitmentService.getCountByStatus("SELECTED");
        long rejected = recruitmentService.getCountByStatus("REJECTED");
        long offerSent = recruitmentService.getCountByStatus("OFFER_SENT");

        Map<String, Long> distribution = recruitmentService.getStatusCounts();

        List<String> labels = distribution.keySet().stream().sorted().collect(Collectors.toList());
        List<Long> values = labels.stream().map(distribution::get).collect(Collectors.toList());

        Map<String, Object> resp = Map.of(
                "total", total,
                "applied", applied,
                "shortlisted", shortlisted,
                "interviewScheduled", interviewScheduled,
                "selected", selected,
                "rejected", rejected,
                "offerSent", offerSent,
                "labels", labels,
                "values", values,
                "displayNames", recruitmentService.getStatusDisplayNames()
        );

        return ResponseEntity.ok(resp);
    }

}
