package com.hr.hrapp.controller;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.Leave;
import com.hr.hrapp.entity.Salary;
import com.hr.hrapp.entity.Timesheet;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.repository.LeaveRepository;
import com.hr.hrapp.repository.EmployeeAttendanceRepository;
import com.hr.hrapp.repository.SalaryRepository;
import com.hr.hrapp.repository.TimesheetRepository;
import com.hr.hrapp.service.EmailService;
import com.hr.hrapp.service.EmployeeSalaryService;
import com.hr.hrapp.service.EmployeeService;
import com.hr.hrapp.service.FinancialService;
import com.hr.hrapp.service.LeaveService;
import com.hr.hrapp.service.LocationService;

@Controller
@RequestMapping("/user")   // ✅ VERY IMPORTANT
public class UserController {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmployeeAttendanceRepository attendanceRepository;

    @Autowired
    private TimesheetRepository timesheetRepository;

    @Autowired
    private SalaryRepository salaryRepository;

    @Autowired
    private LocationService locationService;
    
    @Autowired
    private EmployeeService employeeservice;
    
    @Autowired
    private EmployeeSalaryService employeeSalaryService;
    
    @Autowired
    private FinancialService financialService;
    
    @Autowired
    private LeaveRepository leaveRepository;
    
    @Autowired
    private LeaveService leaveService;
    
    @Autowired
    private EmailService emailService;
    
    

    // ================= DASHBOARD =================
    @GetMapping("/dashboard")
    public String dashboard(Model model, Principal principal) {
    	
    	
    	
        String username = principal.getName();
        Employee emp = employeeRepository.findByEmail(username);

        if (emp == null) {
            throw new RuntimeException("Employee not found: " + username);
        }

        model.addAttribute("employee", emp);
        model.addAttribute("username", emp.getName());

        return "dashboard";
    }

    // ================= PROFILE =================
    @GetMapping("/profile")
    public String profile(Model model, Principal principal) {

        String username = principal.getName();
        Employee emp = employeeRepository.findByEmail(username);

        if (emp == null) {
            throw new RuntimeException("Employee not found: " + username);
        }

        List<Salary> salaries =
                salaryRepository.findByEmployeeId(emp.getEmpId());
        List<Map<String, Object>> processed =
                financialService.processSalary(salaries);

        model.addAttribute("salaryList", processed);
        String exp = employeeSalaryService.calculateExperience(emp.getJoiningDate());
        
        model.addAttribute("employee", emp);
        model.addAttribute("salaries", salaries);
        model.addAttribute("experience", exp);
        

        return "user-profile";
    }

    // ================= TIMESHEET =================
    @GetMapping("/timesheet")
    public String showTimesheet(Model model, Principal principal) {

        String username = principal.getName();
        Employee emp = employeeRepository.findByEmail(username);

        if (emp == null) {
            throw new RuntimeException("Employee not found: " + username);
        }

        model.addAttribute("employee", emp);

        // ===== EXISTING TIMESHEET LOGIC =====
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        List<LocalDate> weekDates = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            weekDates.add(startOfWeek.plusDays(i));
        }

        List<Timesheet> list =
                timesheetRepository.findByEmployeeIdAndDateBetween(
                        emp.getEmpId(), startOfWeek, endOfWeek);

        Map<String, Integer> weekData = new HashMap<>();
        Map<String, String> weekLocation = new HashMap<>();

        for (Timesheet t : list) {
            String day = t.getDate().getDayOfWeek().toString();
            weekData.put(day, t.getHours());
            weekLocation.put(day, t.getWorkLocation());
        }

        model.addAttribute("weekDates", weekDates);
        model.addAttribute("weekData", weekData);
        model.addAttribute("weekLocation", weekLocation);

        // ==================================================
        // 🔥 ADD THIS PART (LEAVES + BALANCE)
        // ==================================================

        List<Leave> leaves = leaveRepository.findByEmpId(emp.getEmpId());

        Set<LocalDate> leaveDates = leaves.stream()
                .map(Leave::getDate)
                .collect(Collectors.toSet());

        model.addAttribute("leaveDates", leaveDates);
        model.addAttribute("leaves", leaves);

        // 🔥 ADD THIS HERE ↓↓↓
        Map<String, Leave> leaveMap = new HashMap<>();
        
        for (DayOfWeek d : DayOfWeek.values()) {

            leaveMap.put(d.toString(), null);

        }


        for (Leave l : leaves) {
            DayOfWeek day = l.getDate().getDayOfWeek();
            leaveMap.put(day.toString(), l);
        }

        model.addAttribute("leaveMap", leaveMap);

        // 👉 balances
        model.addAttribute("sickLeaves", emp.getSickLeaves());
        int monthsThisYear = LocalDate.now().getMonthValue();
        int earnedLeaves = monthsThisYear * 2;

        
        System.out.println("Earned Leaves: " + earnedLeaves );
        

        model.addAttribute("annualLeaves", earnedLeaves);
        model.addAttribute("employee", emp);
        

        // 👉 used & remaining (optional but useful)
        long usedLeaves = leaves.stream()
                .filter(l -> "APPROVED".equals(l.getStatus()))
                .count();

        int totalLeaves = 24;
        int remainingLeaves = totalLeaves - (int) usedLeaves;

        model.addAttribute("usedLeaves", usedLeaves);
        

        return "timesheet";
    }

    // ================= SAVE TIMESHEET =================
    @PostMapping("/timesheet")
    public String saveTimesheet(
            @RequestParam("date") String date,
            @RequestParam("workLocation") String location,
            @RequestParam(value = "hours", required = false) Integer hours,
            @RequestParam(value = "training", required = false) Integer training,
            Principal principal) {

        // ✅ NULL HANDLE (VERY IMPORTANT)
        if (hours == null) hours = 0;
        if (training == null) training = 0;

        // ✅ USER FETCH
        String username = principal.getName();
        Employee emp = employeeRepository.findByEmail(username);

        // ✅ DATE PARSE
        LocalDate localDate = LocalDate.parse(date);

        // ✅ CHECK EXISTING ENTRY
        Timesheet existing = timesheetRepository
                .findByEmployeeIdAndDate(emp.getEmpId(), localDate)
                .orElse(null);

        if (existing != null) {
            // ✅ UPDATE
            existing.setHours(hours);
            existing.setTraining(training);
            existing.setWorkLocation(location);

            timesheetRepository.save(existing);

        } else {
            // ✅ NEW ENTRY
            Timesheet newEntry = new Timesheet();
            newEntry.setEmployeeId(emp.getEmpId());
            newEntry.setDate(localDate);
            newEntry.setHours(hours);
            newEntry.setTraining(training);
            newEntry.setWorkLocation(location);

            timesheetRepository.save(newEntry);
        }

        // ✅ IMPORTANT (RETURN FIX)
        return "redirect:/user/fill-timesheet";
    }

    // ================= EDIT TIMESHEET =================
    @GetMapping("/edit-timesheet")
    public String editTimesheet(@RequestParam("date") String date,
                               Model model,
                               Principal principal) {

        String username = principal.getName();
        Employee emp = employeeRepository.findByEmail(username);

        LocalDate selectedDate = LocalDate.parse(date);

        Timesheet ts = timesheetRepository
                .findByEmployeeIdAndDate(emp.getEmpId(), selectedDate)
                .orElse(new Timesheet());

        model.addAttribute("timesheet", ts);
        model.addAttribute("date", selectedDate);
        model.addAttribute("employee", emp);

        return "edit-timesheet";
    }

    // ================= UPDATE TIMESHEET =================
    @PostMapping("/update-timesheet")
    public String updateTimesheet(@ModelAttribute Timesheet ts, Principal principal) {

        String username = principal.getName();
        Employee emp = employeeRepository.findByEmail(username);

        ts.setEmployeeId(emp.getEmpId());

        timesheetRepository.save(ts);

        return "redirect:/user/timesheet";
    }

    // ================= FILL TIMESHEET =================
    @GetMapping("/fill-timesheet")
    public String fillTimesheetPage(Model model, Principal principal) {

        // ===== EMPLOYEE =====
        String username = principal.getName();
        Employee emp = employeeRepository.findByEmail(username);
        
        model.addAttribute("employee", emp);

        // ✅ ADD THIS (AUTO LOCATION)
        model.addAttribute("employeeLocation", emp.getLocation());

        // ===== STEP 1: Fetch wide range =====
        List<Timesheet> list =
                timesheetRepository.findByEmployeeIdAndDateBetween(
                        emp.getEmpId(),
                        LocalDate.now().minusDays(30),
                        LocalDate.now().plusDays(30)
                );

        // ===== STEP 2: Decide week =====
        

        LocalDate startDate = LocalDate.now().with(DayOfWeek.MONDAY);

        LocalDate endDate = startDate.plusDays(6);

        // ===== STEP 3: Fetch that week =====
        list = timesheetRepository.findByEmployeeIdAndDateBetween(
                emp.getEmpId(),
                startDate,
                endDate
        );

        // ===== STEP 4: Map data =====
        Map<LocalDate, Timesheet> timesheetMap = new HashMap<>();

        for (Timesheet ts : list) {
            timesheetMap.put(ts.getDate(), ts);
        }

        // ===== STEP 5: Prepare week =====
        List<LocalDate> weekDates = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate date = startDate.plusDays(i);
            weekDates.add(date);

            Timesheet ts = timesheetMap.get(date);

            if (ts == null) {
                ts = new Timesheet();
                ts.setDate(date);

                // ✅ DEFAULT LOCATION AUTO FILL
                ts.setWorkLocation(emp.getLocation());
            }

            timesheetMap.put(date, ts);
        }

        // ===== STEP 6: SEND TO UI =====
        model.addAttribute("timesheetMap", timesheetMap);
        model.addAttribute("weekDates", weekDates);
        model.addAttribute("locations", locationService.getAllLocations());

        // ===== LEAVES =====
        List<Leave> leaves = leaveRepository.findByEmpId(emp.getEmpId());

        Set<LocalDate> leaveDates = leaves.stream()
                .map(Leave::getDate)
                .collect(Collectors.toSet());

        model.addAttribute("leaveDates", leaveDates);

        return "fill-timesheet";
    }
    
    @PostMapping("/apply-leave")
    public String applyLeave(@RequestParam String date,
                             @RequestParam String type,
                             Principal principal) {

        Employee emp = employeeRepository.findByEmail(principal.getName());

        // 🔥 Update accrual first
        leaveService.accrueLeaves(emp);

        LocalDate leaveDate = LocalDate.parse(date);

        // prevent duplicate
        if (leaveRepository.existsByEmpIdAndDateAndStatus(emp.getEmpId(), leaveDate, "APPROVED")) {
            return "redirect:/user/timesheet?error=Already applied";
        }

        // balance check
        if ("SICK".equals(type) && emp.getSickLeaves() <= 0) {
            return "redirect:/user/timesheet?error=No sick leaves";
        }

        if ("ANNUAL".equals(type) && emp.getAnnualLeaves() < 1) {
            return "redirect:/user/timesheet?error=No annual leaves";
        }

        Leave leave = new Leave();
        leave.setEmpId(emp.getEmpId());
        leave.setDate(leaveDate);
        leave.setType(type);
        leave.setStatus("PENDING");
        System.out.println("Leave Apply Started");
        leaveRepository.save(leave);
        System.out.println("Leave saved successfully");

        Employee manager = emp.getManager();

        if (manager != null) {

            emailService.sendMail(
                manager.getEmail(),
                "New Leave Request",
                emp.getName() + " applied for " + type + " leave"
            );
        }

        return "redirect:/user/timesheet";
    }

    // ================= SEARCH =================
    @GetMapping("/search")
    public String search(@RequestParam("query") String query) {

        if (query.equalsIgnoreCase("profile")) {
            return "redirect:/user/profile";
        }

        if (query.equalsIgnoreCase("timesheet")) {
            return "redirect:/user/timesheet";
        }

        return "redirect:/user/dashboard";
    }
    
    @GetMapping("/test")
    @ResponseBody
    public String testMail() {

        Salary salary = new Salary();

        salary.setMonth("May");
        salary.setBasicSalary(30000);
        salary.setNetSalary(35000);
        salary.setHikeAmount(5000);

        emailService.sendSalaryMail(
            "sudheerdevalla950214@gmail.com",
            salary
        );

        return "Mail Sent Successfully";
    }
}