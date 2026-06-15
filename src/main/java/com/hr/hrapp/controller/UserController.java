package com.hr.hrapp.controller;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.web.multipart.MultipartFile;
import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.EmployeeDocument;
import com.hr.hrapp.entity.Holiday;
import com.hr.hrapp.entity.Leave;
import com.hr.hrapp.entity.Notification;
import com.hr.hrapp.entity.Salary;
import com.hr.hrapp.entity.Timesheet;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.repository.HolidayRepository;
import com.hr.hrapp.repository.LeaveRepository;
import com.hr.hrapp.repository.NotificationRepository;
import com.hr.hrapp.repository.CompanyUpdateRepository;
import com.hr.hrapp.repository.EmployeeAttendanceRepository;
import com.hr.hrapp.repository.EmployeeDocumentRepository;
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
    private com.hr.hrapp.service.TimesheetValidationService timesheetValidationService;
    
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
    
    @Autowired
    private CompanyUpdateRepository companyUpdateRepository;
    
    @Autowired
    private EmployeeDocumentRepository
            employeeDocumentRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private HolidayRepository holidayRepository;

    
    
    

    // ================= DASHBOARD =================
    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            Principal principal) {

        model.addAttribute(
                "updates",
                companyUpdateRepository.findAll());

        if (principal == null) {
            return "redirect:/login";
        }

        String username =
                principal.getName();

        Employee emp =
                employeeRepository.findByEmail(
                        username);

        if (emp == null) {
            throw new RuntimeException(
                    "Employee not found: "
                            + username);
        }

        model.addAttribute(
                "employee",
                emp);

        model.addAttribute(
                "username",
                emp.getName());

        // 🔔 Notification Count Debug
        System.out.println(
                "Employee ID = " + emp.getEmpId());

        long unreadCount =
                notificationRepository
                        .countByEmployeeIdAndIsReadFalse(
                                emp.getEmpId());

        System.out.println(
                "Unread Count = " + unreadCount);

        model.addAttribute(
                "unreadCount",
                unreadCount);

        // Dashboard Summary Cards
        model.addAttribute(
                "attendancePercentage",
                96);

        model.addAttribute(
                "leaveBalance",
                8);

        model.addAttribute(
                "pendingLeaves",
                2);

        model.addAttribute(
                "travelRequests",
                1);
        List<Holiday> holidays =
                holidayRepository.findAll();

        model.addAttribute(
                "holidays",
                holidays);

        return "dashboard";
    }


    // ================= PROFILE =================
    @GetMapping("/profile")
    public String profile(Model model,
                          Principal principal) {

        // =========================
        // LOGIN CHECK
        // =========================

        if(principal == null) {

            return "redirect:/login";
        }

        String username =
                principal.getName();

        Employee emp =
                employeeRepository
                .findByEmail(username);

        if(emp == null) {

            throw new RuntimeException(
                    "Employee not found: "
                    + username);
        }

        List<Salary> salaries =
                salaryRepository
                .findByEmployeeId(
                        emp.getEmpId());

        List<Map<String, Object>> processed =
                financialService
                .processSalary(salaries);

        model.addAttribute(
                "salaryList",
                processed);

        String exp =
                employeeSalaryService
                .calculateExperience(
                        emp.getJoiningDate());

        model.addAttribute(
                "employee",
                emp);

        model.addAttribute(
                "salaries",
                salaries);

        model.addAttribute(
                "experience",
                exp);

        return "user-profile";
    }

    // ================= TIMESHEET =================
    @GetMapping("/timesheet")
    public String showTimesheet(Model model, Principal principal) {
    	
    	if(principal == null) {
    	    System.out.println("Principal is NULL");
    	    return "redirect:/login";
    	}

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
        
        System.out.println("Annual Leaves From DB = "
                + emp.getAnnualLeaves());

        model.addAttribute(
                "annualLeaves",
                emp.getAnnualLeaves());
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
            @RequestParam(value = "clientName", required = false) String clientName,
            @RequestParam(value = "projectName", required = false) String projectName,
            @RequestParam(value = "workDescription", required = false) String workDescription,
            @RequestParam(value = "latitude", required = false) Double latitude,
            @RequestParam(value = "longitude", required = false) Double longitude,
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
            existing.setClientName(clientName);
            existing.setProjectName(projectName);
            existing.setWorkDescription(workDescription);
            existing.setLatitude(latitude);
            existing.setLongitude(longitude);

            String detectedCity = locationService.getCity(latitude, longitude);

            existing.setExpectedLocation(emp.getLocation());
            existing.setActualLocation(detectedCity);

            // Use new validation service (Haversine -> Location table). Falls back to city-match if needed.
            timesheetValidationService.validateAndNotify(existing, emp);

            timesheetRepository.save(existing);

        } else {
            // ✅ NEW ENTRY
            Timesheet newEntry = new Timesheet();
            newEntry.setEmployeeId(emp.getEmpId());
            newEntry.setDate(localDate);
            
            newEntry.setCreatedAt(
                    LocalDateTime.now());
            newEntry.setHours(hours);
            newEntry.setTraining(training);
            newEntry.setWorkLocation(location);
            newEntry.setClientName(clientName);
            newEntry.setProjectName(projectName);
            newEntry.setWorkDescription(workDescription);
            newEntry.setLatitude(latitude);
            newEntry.setLongitude(longitude);

            String detectedCity = locationService.getCity(latitude, longitude);

            newEntry.setExpectedLocation(emp.getLocation());
            newEntry.setActualLocation(detectedCity);

            // Validate using Haversine and notify on mismatch
            timesheetValidationService.validateAndNotify(newEntry, emp);

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
        
        model.addAttribute(
                "today",
                LocalDate.now());
        
     // ===== HOLIDAYS =====
        List<Holiday> holidays =
                holidayRepository.findAll();

        Map<LocalDate, Holiday> holidayMap =
                new HashMap<>();

        for(Holiday holiday : holidays){

            holidayMap.put(
                    holiday.getHolidayDate(),
                    holiday);
        }

        model.addAttribute(
                "holidayMap",
                holidayMap);
        
        model.addAttribute(
                "holidays",
                holidays);


        return "fill-timesheet";
    }
    
    @PostMapping("/apply-leave")
    public String applyLeave(@RequestParam String date,
                             @RequestParam String type,
                             Principal principal) {
    	
    	System.out.println("APPLY LEAVE METHOD HIT");

        Employee emp = employeeRepository.findByEmail(principal.getName());
        
        System.out.println("Employee = " + emp.getName());
        System.out.println("Annual Leaves = " + emp.getAnnualLeaves());
        System.out.println("Sick Leaves = " + emp.getSickLeaves());

        // 🔥 Update accrual first
        leaveService.accrueLeaves(emp);
        
        System.out.println("After Accrual");
        System.out.println("Annual Leaves = " + emp.getAnnualLeaves());
        System.out.println("Sick Leaves = " + emp.getSickLeaves());
        System.out.println("Leave Type = " + type);

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
    @PostMapping("/upload-photo")
    public String uploadPhoto(
            @RequestParam("photo") MultipartFile photo,
            Principal principal) {

        try {

            String username = principal.getName();

            Employee emp =
                    employeeRepository.findByEmail(username);

            if (emp == null) {
                return "redirect:/user/profile";
            }

            String fileName =
                    System.currentTimeMillis()
                    + "_"
                    + photo.getOriginalFilename();

            Path uploadPath =
                    Paths.get("uploads/profile");

            Files.createDirectories(uploadPath);

            Files.copy(
                    photo.getInputStream(),
                    uploadPath.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING
            );

            emp.setProfilePhoto(fileName);

            employeeRepository.save(emp);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return "redirect:/user/profile";
    }
    @GetMapping("/documents")
    public String documents(Model model,
                            Principal principal) {
    	 if (principal == null) {
    	        return "redirect:/login";
    	    }

        Employee emp =
                employeeRepository.findByEmail(
                        principal.getName());

        List<EmployeeDocument> docs =
                employeeDocumentRepository
                .findByEmployeeId(
                        Long.valueOf(emp.getEmpId()));

        System.out.println("DOC COUNT = " + docs.size());

        EmployeeDocument aadhaar = docs.stream()
                .filter(d ->
                    d.getDocumentType() != null &&
                    d.getDocumentType().equalsIgnoreCase("AADHAAR"))
                .findFirst()
                .orElse(null);

        System.out.println("AADHAAR = " + aadhaar);
        
        EmployeeDocument pan = docs.stream()
                .filter(d ->
                    d.getDocumentType() != null &&
                    d.getDocumentType().equalsIgnoreCase("PAN"))
                .findFirst()
                .orElse(null);

        EmployeeDocument offer = docs.stream()
                .filter(d ->
                    d.getDocumentType() != null &&
                    d.getDocumentType().equalsIgnoreCase("OFFER"))
                .findFirst()
                .orElse(null);

        EmployeeDocument certificate = docs.stream()
                .filter(d ->
                    d.getDocumentType() != null &&
                    d.getDocumentType().equalsIgnoreCase("CERTIFICATE"))
                .findFirst()
                .orElse(null);

        model.addAttribute("pan", pan);
        model.addAttribute("offer", offer);
        model.addAttribute("certificate", certificate);

        model.addAttribute("aadhaar", aadhaar);
        

        return "my-documents";
    }
    @PostMapping("/upload-document")
    public String uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType,
            Principal principal) {

        try {

            Employee emp =
                    employeeRepository.findByEmail(
                            principal.getName());

            if (emp == null) {
                return "redirect:/user/documents";
            }

            String fileName =
                    System.currentTimeMillis()
                    + "_"
                    + file.getOriginalFilename();

            Path uploadPath =
                    Paths.get("uploads/documents");

            Files.createDirectories(uploadPath);

            Files.copy(
                    file.getInputStream(),
                    uploadPath.resolve(fileName),
                    StandardCopyOption.REPLACE_EXISTING
            );

            EmployeeDocument doc =
                    new EmployeeDocument();

            doc.setEmployeeId(
                    Long.valueOf(emp.getEmpId()));

            doc.setDocumentType(documentType);

            doc.setFileName(fileName);

            employeeDocumentRepository.save(doc);

        } catch (Exception e) {

            e.printStackTrace();
        }

        return "redirect:/user/documents?success";
    }
    @GetMapping("/download-document/{id}")
    public ResponseEntity<Resource> downloadDocument(
            @PathVariable Long id) throws IOException {

        EmployeeDocument doc =
                employeeDocumentRepository
                .findById(id)
                .orElseThrow();

        Path filePath =
                Paths.get("uploads/documents")
                .resolve(doc.getFileName());

        Resource resource =
                new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                        doc.getFileName() + "\"")
                .body(resource);
    }
    @GetMapping("/notifications")
    public String notifications(
            Principal principal,
            Model model) {

        Employee emp =
                employeeRepository.findByEmail(
                        principal.getName());

        List<Notification> notifications =
                notificationRepository
                .findByEmployeeIdOrderByCreatedAtDesc(
                        emp.getEmpId());

        // Mark all as read
        for(Notification n : notifications){
            n.setRead(true);
        }

        notificationRepository.saveAll(
                notifications);

        model.addAttribute(
                "notifications",
                notifications);

        return "notifications";
    }
    @GetMapping("/holidays")
    public String holidays(Model model) {

        List<Holiday> holidays =
                holidayRepository.findAll();

        model.addAttribute(
                "holidays",
                holidays);

        return "holidays";
    }
    
}