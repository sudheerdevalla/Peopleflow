package com.hr.hrapp.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.Notification;
import com.hr.hrapp.entity.TravelRequest;
import com.hr.hrapp.entity.TravelAudit;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.repository.NotificationRepository;
import com.hr.hrapp.repository.TravelRequestRepository;
import java.io.File;
import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/travel")
public class TravelController {

    @Autowired
    private TravelRequestRepository travelRepository;

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private com.hr.hrapp.repository.TravelAuditRepository travelAuditRepository;

    @Autowired
    private com.hr.hrapp.service.EmailService emailService;

    // ✅ Open Apply Form
    @GetMapping("/apply")
    public String applyTravelForm(Model model) {

        model.addAttribute(
                "travelRequest",
                new TravelRequest());

        return "apply-travel";
    }

    // ✅ Save Travel Request
    @PostMapping("/save")
    public String saveTravelRequest(
            @ModelAttribute TravelRequest request,
            @RequestParam("ticket") MultipartFile file,
            Principal principal) throws IOException {

        String email = principal.getName();

        Employee employee =
                employeeRepository.findByEmail(email);

        request.setEmpId(employee.getEmpId());

        request.setEmployeeName(employee.getName());

        // Validate dates
        if (request.getFromDate() != null && request.getToDate() != null) {
            if (request.getFromDate().isAfter(request.getToDate())) {
                return "redirect:/travel/apply?error=InvalidDates";
            }
        }

        // Prevent overlapping/duplicate requests
        List<TravelRequest> overlaps = travelRepository.findOverlappingRequests(employee.getEmpId(), request.getFromDate(), request.getToDate(), null);
        if (overlaps != null && !overlaps.isEmpty()) {
            return "redirect:/travel/apply?error=DuplicateRequest";
        }

        request.setStatus("REQUESTED");

        // ✅ File Upload
        if(!file.isEmpty()){

            String fileName =
                    System.currentTimeMillis()
                    + "_"
                    + file.getOriginalFilename();

            String uploadDir =
                    System.getProperty("user.dir")
                    + "/uploads/";

            File uploadPath =
                    new File(uploadDir);

            if(!uploadPath.exists()){

                uploadPath.mkdirs();
            }

            file.transferTo(
                    new File(uploadDir + fileName)
            );

            request.setTicketFile(fileName);
        }

        travelRepository.save(request);

        // Audit
        TravelAudit audit = new TravelAudit();
        audit.setTravelRequestId(request.getId());
        audit.setPerformedByEmployeeId(employee.getEmpId());
        audit.setAction("REQUESTED");
        audit.setComments("Created by employee");
        travelAuditRepository.save(audit);

        // Notify manager
        Employee manager = employee.getManager();
        if (manager != null) {
            Notification n = new Notification();
            n.setEmployeeId(manager.getEmpId());
            n.setMessage("New travel request from " + employee.getName());
            n.setRead(false);
            n.setCreatedAt(java.time.LocalDateTime.now());
            notificationRepository.save(n);

            // Send email to manager
            String managerBody = "<h3>New Travel Request</h3><p>Employee: " + employee.getName() + "</p>" +
                    "<p>Destination: " + request.getDestination() + "</p>" +
                    "<p>From: " + request.getFromDate() + " To: " + request.getToDate() + "</p>";
            emailService.sendMail(manager.getEmail(), "New Travel Request", managerBody);
        }

        return "redirect:/travel/my-requests";
    }

    // ✅ Employee Requests
    @GetMapping("/my-requests")
    public String myTravelRequests(
            Principal principal,
            Model model) {

        String email = principal.getName();

        Employee employee =
                employeeRepository.findByEmail(email);

        List<TravelRequest> requests =
                travelRepository.findByEmpId(
                        employee.getEmpId());

        model.addAttribute("requests", requests);

        return "my-travel-requests";
    }

    // ✅ Manager View Requests
    @GetMapping("/manager")
    public String managerTravelRequests(
            Principal principal,
            Model model) {

        if(principal == null){
            return "redirect:/login";
        }

        String email = principal.getName();

        Employee manager =
                employeeRepository.findByEmail(email);

        List<Employee> employees =
                employeeRepository.findByManager_EmpId(
                        manager.getEmpId());

        List<TravelRequest> managerRequests =
                new ArrayList<>();

        for(Employee emp : employees){

            managerRequests.addAll(
                    travelRepository.findByEmpId(
                            emp.getEmpId()
                    )
            );
        }

        model.addAttribute(
                "requests",
                managerRequests
        );

        return "manager-travel-requests";
    }

    // ✅ Manager Approve
    @GetMapping("/manager-approve/{id}")
    public String managerApprove(
            @PathVariable Long id) {

        TravelRequest request =
                travelRepository.findById(id)
                        .orElse(null);

        if(request != null){

            request.setStatus("MANAGER_APPROVED");

            travelRepository.save(request);

            // Audit
            TravelAudit audit = new TravelAudit();
            audit.setTravelRequestId(request.getId());
            audit.setPerformedByEmployeeId(request.getEmpId());
            audit.setAction("MANAGER_APPROVED");
            audit.setComments("Approved by manager");
            travelAuditRepository.save(audit);

            // Notify employee
            Notification n = new Notification();
            n.setEmployeeId(request.getEmpId());
            n.setMessage("Your travel request has been approved by manager.");
            n.setRead(false);
            n.setCreatedAt(java.time.LocalDateTime.now());
            notificationRepository.save(n);

            // Send email to employee
            Employee emp = employeeRepository.findByEmail("");
            // find employee by id
            Employee employee = employeeRepository.findById(request.getEmpId()).orElse(null);
            if (employee != null) {
                String body = "<p>Your travel request to " + request.getDestination() + " has been approved by your manager.</p>";
                emailService.sendMail(employee.getEmail(), "Travel Request Manager Approved", body);
            }
        }

        return "redirect:/travel/manager";
    }

    // ✅ Manager Reject
    @GetMapping("/manager-reject/{id}")
    public String managerReject(
            @PathVariable Long id) {

        TravelRequest request =
                travelRepository.findById(id)
                        .orElse(null);

        if(request != null){

            request.setStatus("REJECTED");

            travelRepository.save(request);

            TravelAudit audit = new TravelAudit();
            audit.setTravelRequestId(request.getId());
            audit.setPerformedByEmployeeId(request.getEmpId());
            audit.setAction("REJECTED");
            audit.setComments("Rejected by manager");
            travelAuditRepository.save(audit);

            // Notify employee
            Notification n = new Notification();
            n.setEmployeeId(request.getEmpId());
            n.setMessage("Your travel request has been rejected by manager.");
            n.setRead(false);
            n.setCreatedAt(java.time.LocalDateTime.now());
            notificationRepository.save(n);
        }

        return "redirect:/travel/manager";
    }

    // ✅ HR/Admin View
    @GetMapping("/admin")
    public String adminTravelRequests(Model model) {

        List<TravelRequest> requests = travelRepository.findByStatus("MANAGER_APPROVED");

        model.addAttribute(
                "requests",
                requests);

        return "admin-travel-requests";
    }

    // ✅ HR Final Approval
    @GetMapping("/approve/{id}")
    public String approveTravel(
            @PathVariable Long id) {

        TravelRequest request =
                travelRepository.findById(id)
                        .orElse(null);

        if (request != null) {
            request.setStatus("ADMIN_APPROVED");

            travelRepository.save(request);

            TravelAudit audit = new TravelAudit();
            audit.setTravelRequestId(request.getId());
            audit.setPerformedByEmployeeId(request.getEmpId());
            audit.setAction("ADMIN_APPROVED");
            audit.setComments("Approved by admin");
            travelAuditRepository.save(audit);

            Notification n = new Notification();
            n.setEmployeeId(request.getEmpId());
            n.setMessage("Your travel request has been approved by admin.");
            n.setRead(false);
            n.setCreatedAt(java.time.LocalDateTime.now());
            notificationRepository.save(n);

            // Optionally mark as COMPLETED when travel end date passed etc. (not automatic here)
        }

        return "redirect:/travel/admin";
    }

    // ✅ HR Reject
    @GetMapping("/reject/{id}")
    public String rejectTravel(
            @PathVariable Long id) {

        TravelRequest request =
                travelRepository.findById(id)
                        .orElse(null);
        if (request != null) {

            request.setStatus("REJECTED");

            travelRepository.save(request);

            TravelAudit audit = new TravelAudit();
            audit.setTravelRequestId(request.getId());
            audit.setPerformedByEmployeeId(request.getEmpId());
            audit.setAction("REJECTED");
            audit.setComments("Rejected by admin");
            travelAuditRepository.save(audit);

            Notification n = new Notification();
            n.setEmployeeId(request.getEmpId());
            n.setMessage("Your travel request has been rejected by admin.");
            n.setRead(false);
            n.setCreatedAt(java.time.LocalDateTime.now());
            notificationRepository.save(n);
        }

        return "redirect:/travel/admin";
    }
}