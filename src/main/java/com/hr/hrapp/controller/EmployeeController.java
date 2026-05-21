package com.hr.hrapp.controller;


import java.security.Principal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.EmployeeAttendance;
import com.hr.hrapp.entity.Leave;
import com.hr.hrapp.repository.EmployeeAttendanceRepository;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.repository.LeaveRepository;
import com.hr.hrapp.service.EmailService;
import com.hr.hrapp.service.EmployeeSalaryService;
import com.hr.hrapp.service.LeaveService;
import com.hr.hrapp.service.AuditLogService;
import com.hr.hrapp.entity.AuditLog;

import jakarta.servlet.http.HttpServletRequest;

import com.hr.hrapp.dto.EmployeePayslip;

@Controller
@RequestMapping("/admin")
public class EmployeeController {

    @Autowired
    private EmployeeSalaryService service;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private EmployeeAttendanceRepository attendanceRepository;
    
    @Autowired
    private LeaveRepository leaveRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private AuditLogService auditLogService;
    
    @GetMapping
    @PreAuthorize("hasAuthority('READ_EMPLOYEE')")
    public List<Employee> getAllEmployee(){
    	return service.getAllEmployees();
    }

    @GetMapping("/salary/{id}")
    public double getSalary(@PathVariable Long id) {
        Employee emp = service.getEmployeeById(id);
        return service.calculateSalary(emp, 0).getNetSalary();
    }
    @GetMapping("/{id}/payslip")
    @PreAuthorize("hasAuthority('READ_EMPLOYEE')")
    public EmployeePayslip getPayslip(@PathVariable Long id) {
    	return service.generatePayslip(id);
    }
    	
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_EMPLOYEE')")
    public String deleteEmployee(@PathVariable Long id, HttpServletRequest request) {
        service.deleteEmployee(id);
        auditLogService.save(new AuditLog(request.getUserPrincipal().getName(), "DELETE_EMPLOYEE", request.getRequestURI(), LocalDateTime.now(), "SUCCESS"));
        return "Employee Deleted"; 
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WRITE_EMPLOYEE')")
    public Employee updateEmployee(
            @PathVariable Long id,
            @RequestBody Employee emp,
            HttpServletRequest request) {
        Employee updated = service.updateEmployee(id, emp);
        auditLogService.save(new AuditLog(request.getUserPrincipal().getName(), "UPDATE_EMPLOYEE", request.getRequestURI(), LocalDateTime.now(), "SUCCESS"));
        return updated;
    }
    @GetMapping("/payslip/pdf/{id}")
    @PreAuthorize("hasAuthority('READ_EMPLOYEE')")
    public ResponseEntity<byte[]> downloadPayslip(
            @PathVariable Long id,
            @RequestParam int leaves) {

        byte[] pdf = service.generatePayslipPdf(id, leaves);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=payslip.pdf")
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }
    @GetMapping("/edit/{id}")
    public String editEmployee(@PathVariable Long id, Model model) {

        Employee emp = employeeRepository.findById(id).orElse(null);

        model.addAttribute("employee", emp);

        model.addAttribute("employees",
                employeeRepository.findAll());

        return "edit-employee";
    }

    @PostMapping("/update")
    public String updateEmployee(@ModelAttribute Employee employee) {
        employeeRepository.save(employee);
        return "redirect:/admin/employees";
    }
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('WRITE_EMPLOYEE')")
    public String deleteEmployeeById(@PathVariable Long id, HttpServletRequest request) {
        employeeRepository.deleteById(id);
        auditLogService.save(new AuditLog(request.getUserPrincipal().getName(), "DELETE_EMPLOYEE", request.getRequestURI(), LocalDateTime.now(), "SUCCESS"));
        return "redirect:/admin/employees";
    }
    @GetMapping("/update")
    public String handleUpdateGet() {
        return "redirect:/admin/employees";
    }
   /* @GetMapping("/admin/employees")
    public String employeesPage(Model model) {
    	model.addAttribute("employees", employeeRepository.findAll());
        return "employees";
    }*/
    
    
    @GetMapping("/employees")
    public String listEmployees(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<Employee> employeePage =
                employeeRepository.findByStatus("Active", PageRequest.of(page, 5));

        // 🔥 IKKADA START
        List<Employee> employees = employeePage.getContent();

        for (Employee emp : employees) {
            if (emp.getJoiningDate() != null) {
                Period p = Period.between(
                        emp.getJoiningDate(),
                        LocalDate.now()
                );
                String expStr = p.getYears() + " Years "  + p.getMonths() + " Months";
                emp.setExperience(expStr); // display kosam
            }
        }
       

        model.addAttribute("employees", employees);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeePage.getTotalPages());

        return "employees";
    }
    @PostMapping("/save-employees")
    @PreAuthorize("hasAuthority('WRITE_EMPLOYEE')")
    public String saveEmployee(@ModelAttribute Employee employee, RedirectAttributes ra, HttpServletRequest request) {
        employeeRepository.save(employee);
        auditLogService.save(new AuditLog(request.getUserPrincipal().getName(), "CREATE_EMPLOYEE", request.getRequestURI(), LocalDateTime.now(), "SUCCESS"));
        ra.addFlashAttribute("success", "Employee saved successfully!");
        return "redirect:/admin/employees";
    }
  
    @GetMapping("/add-employees")
    public String showAddemployeesForm(Model model) {
    	model.addAttribute("employees", new Employee());
        return "add-employees";
    }
    
    @GetMapping("/attendance")
    public String attendancePage(Model model) {
    	model.addAttribute("attendanceList", attendanceRepository.findAll());
    	model.addAttribute("timesheet", new EmployeeAttendance());
        return "admin-attendance";
    }
    @PostMapping("/mark-attendance")
    @PreAuthorize("hasAuthority('WRITE_EMPLOYEE')")
    public String markAttendance(@RequestParam Long empId,
    		                     @RequestParam int totalDays,
                                 @RequestParam int presentDays) {

        EmployeeAttendance att = new EmployeeAttendance();
        att.setEmployeeId(empId);
        att.setTotalDays(totalDays);
        att.setPresentDays(presentDays);

        attendanceRepository.save(att);

        return "redirect:/admin/attendance";
    }
    @GetMapping("/assign-location/{id}")
    public String assignLocation(@PathVariable Long id, Model model) {

        Employee emp = employeeRepository.findById(id).orElse(null);

        model.addAttribute("employee", emp);

        return "assignlocation"; // HTML file
    }
    @PostMapping("/assign-location")
    @PreAuthorize("hasAuthority('WRITE_EMPLOYEE')")
    public String saveLocation(@RequestParam Long id,
                               @RequestParam String location) {

        Employee emp = employeeRepository.findById(id).orElse(null);

        if (emp != null) {
            emp.setLocation(location);
            employeeRepository.save(emp);
        }

        return "redirect:/admin/employees";
    }
    @GetMapping("/leaves")
   // @PreAuthorize("hasAuthority('READ_EMPLOYEE')")
    public String adminLeaves(Model model) {

        List<Leave> leaves = leaveRepository.findAll();

        model.addAttribute("leaves", leaves);

        return "admin-leaves";
    }
    @GetMapping("/manager/leaves")
    public String managerLeaves(Model model,
                                Principal principal) {

        String username = principal.getName();

        Employee manager =
                employeeRepository
                .findByEmail(username);

        List<Employee> employees =
                employeeRepository
                .findByManager_EmpId(manager.getEmpId());

        List<Leave> managerLeaves =
                new ArrayList<>();

        Map<Long, String> employeeNames =
                new HashMap<>();

        for(Employee emp : employees){

            List<Leave> leaves =
                    leaveRepository
                    .findByEmpId(emp.getEmpId());

            managerLeaves.addAll(leaves);

            employeeNames.put(
                    emp.getEmpId(),
                    emp.getName()
            );
        }

        model.addAttribute(
                "employeeNames",
                employeeNames
        );

        model.addAttribute(
                "leaves",
                managerLeaves
        );

        return "manager-leaves";
    }
    @GetMapping("/approve-leave/{id}")
    @PreAuthorize("hasAuthority('WRITE_EMPLOYEE')")
    public String approveLeave(@PathVariable Long id) {

        Leave leave = leaveRepository.findById(id).orElse(null);

        if (leave == null) {
            return "redirect:/admin/leaves";
        }

        Employee emp = employeeRepository.findById(leave.getEmpId()).orElse(null);

        if (emp != null) {

            if ("SICK".equals(leave.getType())) {
                emp.setSickLeaves(emp.getSickLeaves() - 1);
            } else {
                emp.setAnnualLeaves(emp.getAnnualLeaves() - 1);
            }

            leave.setStatus("APPROVED");
            
            String body = """
            		<html>

            		<body style='font-family:Arial;background:#f4f4f4;padding:20px;'>

            		<div style='max-width:600px;
            		margin:auto;
            		background:white;
            		border-radius:10px;
            		overflow:hidden;'>

            		<div style='background:#0f172a;
                    text-align:center;'>

                   <img src='http://localhost:8080/images/LeaveBanner.PNG'
                   width='600'
                   height='170'
                   style='display:block;
                   width:100%%;
                   max-width:600px;
                   margin:auto;' />

                   </div>

            		<div style='padding:30px;'>

            		<h2 style='color:green;'>
            		Leave Approved ✅
            		</h2>

            		<p>Hello <b>%s</b>,</p>

            		<p>Your leave request has been approved.</p>

            		<h3>Leave Details</h3>

            		<table border='1'
            		cellpadding='10'
            		style='border-collapse:collapse;width:100%%;'>

            		<tr>
            		<td><b>Leave Type</b></td>
            		<td>%s</td>
            		</tr>

            		<tr>
            		<td><b>Leave From</b></td>
            		<td>%s</td>
            		</tr>

            		<tr>
            		<td><b>Leave To</b></td>
            		<td>%s</td>
            		</tr>

            		<tr>
            		<td><b>Approved By</b></td>
            		<td>Manager</td>
            		</tr>

            		</table>

            		<br>

            		<p style='color:gray;font-size:13px;'>
            		This is an auto-generated email.
            		Please do not reply.
            		</p>

            		</div>
            		</div>

            		</body>
            		</html>
            		""".formatted(
            		    emp.getName(),
            		    leave.getType(),
            		    leave.getDate(),
            		    leave.getDate()
            		);

            		emailService.sendMail(
            		    emp.getEmail(),
            		    "Leave Approved",
            		    body
            		);
            leaveRepository.save(leave);
            employeeRepository.save(emp);
        }

        return "redirect:/admin/manager/leaves";
    }
    @GetMapping("/reject-leave/{id}")
    @PreAuthorize("hasAuthority('WRITE_EMPLOYEE')")
    public String rejectLeave(@PathVariable Long id) {

        Leave leave = leaveRepository.findById(id).orElse(null);

        if (leave == null) {
            return "redirect:/admin/leaves";
        }

        Employee emp = employeeRepository
                .findById(leave.getEmpId())
                .orElse(null);

        leave.setStatus("REJECTED");

        String body = """
        		<html>

        		<body style='font-family:Arial;background:#f4f4f4;padding:20px;'>

        		<div style='max-width:600px;
        		margin:auto;
        		background:white;
        		border-radius:10px;
        		overflow:hidden;'>

        		<div style='background:#0f172a;
        		text-align:center;'>

        		    <img src='http://localhost:8080/images/LeaveBanner.PNG'
                    width='600'
                   height='170'
                   style='display:block;
                   width:100%;
                   max-width:600px;
                   margin:auto;' />
        		</div>

        		<div style='padding:30px;'>

        		<h2 style='color:red;'>
        		Leave Rejected ❌
        		</h2>

        		<p>Hello <b>%s</b>,</p>

        		<p>Your leave request has been rejected.</p>

        		<h3>Leave Details</h3>

        		<table border='1'
        		cellpadding='10'
        		style='border-collapse:collapse;width:100%%;'>

        		<tr>
        		<td><b>Leave Type</b></td>
        		<td>%s</td>
        		</tr>

        		<tr>
        		<td><b>Leave Date</b></td>
        		<td>%s</td>
        		</tr>

        		<tr>
        		<td><b>Rejected By</b></td>
        		<td>Manager</td>
        		</tr>

        		</table>

        		<br>

        		<p style='color:gray;font-size:13px;'>
        		This is an auto-generated email.
        		Please do not reply.
        		</p>

        		</div>
        		</div>

        		</body>
        		</html>

        		""".formatted(
        		        emp.getName(),
        		        leave.getType(),
        		        leave.getDate()
        		);

        		emailService.sendMail(
        		        emp.getEmail(),
        		        "Leave Rejected",
        		        body
        		);

        leaveRepository.save(leave);

        return "redirect:/admin/manager/leaves";
    }
}
