package com.hr.hrapp.leave;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.Leave;

class LeaveWorkflowTest {

    @Test
    void testLeaveApprovalFlow() {

        Employee emp = new Employee();
        emp.setEmpId(1L);
        emp.setSickLeaves(6);

        Leave leave = new Leave();
        leave.setEmpId(1L);
        leave.setType("SICK");
        leave.setStatus("PENDING");

        // Manager Approves
        leave.setStatus("APPROVED");
        emp.setSickLeaves(emp.getSickLeaves() - 1);

        assertEquals("APPROVED", leave.getStatus());
        assertEquals(5, emp.getSickLeaves());
    }
}