package com.hr.hrapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hr.hrapp.payroll.report.CEOReportService;

@RestController
public class TestController {

    @Autowired
    private CEOReportService ceoReportService;

    @GetMapping("/test-ceo-report")
    public String testCEOReport() {

        ceoReportService.sendCEOReport();

        return "CEO Report Sent Successfully";
    }
}