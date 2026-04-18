package com.hr.hrapp.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.Salary;
import com.hr.hrapp.entity.Timesheet;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.repository.SalaryRepository;
import com.hr.hrapp.repository.TimesheetRepository;
import com.hr.hrapp.repository.EmployeeAttendanceRepository;

@Controller
public class UserController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeAttendanceRepository attendanceRepository;

    @Autowired
    private TimesheetRepository timesheetRepository;

    @Autowired
    private SalaryRepository salaryRepository;

    // ================= DASHBOARD =================
    @GetMapping("/user/dashboard")
    public String dashboard(Model model, Principal principal) {

        String username = principal.getName();
        Employee emp = employeeRepository.findByEmail(username);

        if (emp == null) {
            throw new RuntimeException("Employee not found: " + username);
        }

        model.addAttribute("employee", emp);
        return "user-dashboard";
    }

    // ================= PROFILE =================
    @GetMapping("/user/profile")
    public String profile(Model model, Principal principal) {

        String username = principal.getName();
        Employee emp = employeeRepository.findByEmail(username);

        if (emp == null) {
            throw new RuntimeException("Employee not found: " + username);
        }

        List<Salary> salaries = salaryRepository.findByEmployeeId(emp.getEmpId());

        model.addAttribute("employee", emp);
        model.addAttribute("salaries", salaries);

        return "user-profile";
    }

    // ================= TIMESHEET PAGE =================
    @GetMapping("/user/timesheet")
    public String showTimesheet(Model model, Principal principal) {

        String username = principal.getName();
        Employee emp = employeeRepository.findByEmail(username);

        if (emp == null) {
            throw new RuntimeException("Employee not found: " + username);
        }

        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(java.time.DayOfWeek.SUNDAY);

        List<Timesheet> list =
                timesheetRepository.findByEmployeeIdAndDateBetween(
                        emp.getEmpId(), startOfWeek, endOfWeek
                );

        Map<String, Integer> weekData = new HashMap<>();

        for (Timesheet t : list) {
            String day = t.getDate().getDayOfWeek().toString();
            weekData.put(day, t.getHours());
        }

        model.addAttribute("weekData", weekData);
        model.addAttribute("timesheet", new Timesheet());

        return "attendance";
    }

    // ================= SAVE TIMESHEET (🔥 UPDATED) =================
    @PostMapping("/user/timesheet")
    public String saveTimesheet(Timesheet timesheet, Principal principal) {

        String username = principal.getName();
        Employee emp = employeeRepository.findByEmail(username);

        if (emp == null) {
            throw new RuntimeException("Employee not found: " + username);
        }

        // 🔹 Set employee id
        timesheet.setEmployeeId(emp.getEmpId());

        // 🔥 IMPORTANT: FORCE ASSIGNED LOCATION
        // (UI or GPS ni ignore chestundi)
        if (emp.getAssignedLocation() != null) {
            timesheet.setWorkLocation(emp.getAssignedLocation());
        } else {
            timesheet.setWorkLocation("Home"); // fallback
        }

        // 🔹 Save
        timesheetRepository.save(timesheet);

        return "redirect:/user/timesheet";
    }

    // ================= FILL TIMESHEET PAGE =================
    @GetMapping("/user/fill-timesheet")
    public String fillTimesheetPage(Model model, Principal principal) {

        // 🔹 Logged-in user
        String username = principal.getName();

        // 🔹 Fetch employee
        Employee emp = employeeRepository.findByEmail(username);

        if (emp == null) {
            throw new RuntimeException("Employee not found: " + username);
        }

        // 🔥 IMPORTANT: send employee to HTML
        model.addAttribute("employee", emp);

        // 🔹 Timesheet object
        model.addAttribute("timesheet", new Timesheet());

        return "fill-timesheet";
    }

    // ================= SEARCH =================
    @GetMapping("/user/search")
    public String search(@RequestParam("query") String query) {

        if (query.equalsIgnoreCase("personal information") ||
                query.equalsIgnoreCase("profile")) {
            return "redirect:/user/profile";
        }

        if (query.equalsIgnoreCase("timesheet") ||
                query.equalsIgnoreCase("time")) {
            return "redirect:/user/timesheet";
        }

        return "redirect:/user/dashboard";
    }
}