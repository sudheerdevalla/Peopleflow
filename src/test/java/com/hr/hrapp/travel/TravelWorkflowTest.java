package com.hr.hrapp.travel;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hr.hrapp.entity.TravelRequest;

class TravelWorkflowTest {

    @Test
    void testTravelApprovalFlow() {

        TravelRequest request = new TravelRequest();

        // Employee Apply
        request.setStatus("REQUESTED");
        assertEquals("REQUESTED", request.getStatus());

        // Manager Approve
        request.setStatus("MANAGER_APPROVED");
        assertEquals("MANAGER_APPROVED", request.getStatus());

        // HR Approve
        request.setStatus("ADMIN_APPROVED");
        assertEquals("ADMIN_APPROVED", request.getStatus());

        // Payroll Processed
        request.setPayrollProcessed(true);
        assertEquals(true, request.isPayrollProcessed());
    }
}