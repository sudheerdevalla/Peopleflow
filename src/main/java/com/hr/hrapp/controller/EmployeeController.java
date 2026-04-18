package com.hr.hrapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.EmployeeAttendance;
import com.hr.hrapp.repository.EmployeeAttendanceRepository;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.service.EmployeeSalaryService;



import com.hr.hrapp.dto.EmployeePayslip;

@Controller

public class EmployeeController {

    @Autowired
    private EmployeeSalaryService service;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private EmployeeAttendanceRepository attendanceRepository;
    
    @GetMapping
    public List<Employee> getAllEmployee(){
    	return service.getAllEmployees();
    }

    @GetMapping("/salary/{id}")
    public double getSalary(@PathVariable Long id) {
        Employee emp = service.getEmployeeById(id);
        return service.calculateSalary(emp, 0).getNetSalary();
    }
    @GetMapping("/{id}/payslip")
    public EmployeePayslip getPayslip(@PathVariable Long id) {
    	return service.generatePayslip(id);
    }
    	
    @DeleteMapping("/{id}")
    public String deleteEmployee(@PathVariable Long id) {
    	service.deleteEmployee(id);
    	return "Employee Deleted"; 
    }
    @PutMapping("/{id}")
    public Employee updateEmployee(
            @PathVariable Long id,
            @RequestBody Employee emp) {

        return service.updateEmployee(id, emp);
    }
    @GetMapping("/payslip/pdf/{id}")
    public ResponseEntity<byte[]> downloadPayslip(
            @PathVariable Long id,
            @RequestParam int leaves) {

        byte[] pdf = service.generatePayslipPdf(id, leaves);

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=payslip.pdf")
                .header("Content-Type", "application/pdf")
                .body(pdf);
    }
    @GetMapping("/admin/edit/{id}")
    public String editEmployee(@PathVariable Long id, Model model) {
        Employee emp = employeeRepository.findById(id).orElse(null);
        model.addAttribute("employee", emp);
        return "edit-employee";
    }

    @PostMapping("/admin/update")
    public String updateEmployee(@ModelAttribute Employee employee) {
        employeeRepository.save(employee);
        return "redirect:/admin/employees";
    }
    @GetMapping("/admin/delete/{id}")
    public String deleteEmployeeById(@PathVariable Long id) {
        employeeRepository.deleteById(id);
        return "redirect:/admin/employees";
    }
   /* @GetMapping("/admin/employees")
    public String employeesPage(Model model) {
    	model.addAttribute("employees", employeeRepository.findAll());
        return "employees";
    }*/
    
    
    @GetMapping("/admin/employees")
    public String listEmployees(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<Employee> employeePage = employeeRepository.findByStatus("Active", PageRequest.of(page, 5));

        model.addAttribute("employees", employeePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", employeePage.getTotalPages());

        return "employees";
    }
    @PostMapping("/admin/save-employees")
    public String saveEmployee(@ModelAttribute Employee employee, RedirectAttributes ra) {
        employeeRepository.save(employee);
        ra.addFlashAttribute("success", "Employee saved successfully!");
        return "redirect:/admin/employees";
    }
  
    @GetMapping("/admin/add-employees")
    public String showAddemployeesForm(Model model) {
    	model.addAttribute("employees", new Employee());
        return "add-employees";
    }
    
    @GetMapping("/admin/attendance")
    public String attendancePage(Model model) {
    	model.addAttribute("attendanceList", attendanceRepository.findAll());
    	model.addAttribute("timesheet", new EmployeeAttendance());
        return "attendance";
    }
    @PostMapping("/admin/mark-attendance")
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
}