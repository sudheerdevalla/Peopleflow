package com.hr.hrapp.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
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

    // ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¦ Open Apply Form
    @GetMapping("/apply")
    public String applyTravelForm(Model model) {

        model.addAttribute(
                "travelRequest",
                new TravelRequest());

        return "apply-travel";
    }

    // ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¦ Save Travel Request
    @PostMapping("/save")
    public String saveTravelRequest(
            @ModelAttribute TravelRequest request,
            @RequestParam(value = "ticket", required = false) MultipartFile file,
            @RequestParam(value = "foodInvoice", required = false) MultipartFile foodInvoice,
            @RequestParam(value = "hotelInvoice", required = false) MultipartFile hotelInvoice,
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

        // ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¦ File Upload
        // Travel mode and reimbursement
        if ("BIKE".equalsIgnoreCase(request.getTravelMode())) {
            if (request.getBikeDistanceKm() != null && request.getBikeDistanceKm() > 0) {
                request.setBikeAmount(request.getBikeDistanceKm() * 3);
            }
        }

        String uploadDir =
                System.getProperty("user.dir")
                + "/uploads/";

        File uploadPath = new File(uploadDir);

        if (!uploadPath.exists()) {
            uploadPath.mkdirs();
        }

        // Bus ticket
        if (file != null && !file.isEmpty()) {
            String fileName =
                    System.currentTimeMillis()
                    + "_"
                    + file.getOriginalFilename();

            file.transferTo(new File(uploadDir + fileName));
            request.setTicketFile(fileName);
        }

        // Food invoice
        if (foodInvoice != null && !foodInvoice.isEmpty()) {
            String fileName =
                    System.currentTimeMillis()
                    + "_food_"
                    + foodInvoice.getOriginalFilename();

            foodInvoice.transferTo(new File(uploadDir + fileName));
            request.setFoodInvoiceFile(fileName);
        }

        // Hotel invoice
        if (hotelInvoice != null && !hotelInvoice.isEmpty()) {
            String fileName =
                    System.currentTimeMillis()
                    + "_hotel_"
                    + hotelInvoice.getOriginalFilename();

            hotelInvoice.transferTo(new File(uploadDir + fileName));
            request.setHotelInvoiceFile(fileName);
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

    // ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¦ Employee Requests
    // ================= MOBILE MY TRAVEL REQUESTS API =================
    @GetMapping("/api/my-requests")
    @ResponseBody
    public ResponseEntity<?> getMyTravelRequests(Principal principal) {
        Employee employee =
                employeeRepository.findByEmail(principal.getName());

        if (employee == null) {
            return ResponseEntity.notFound().build();
        }

        List<TravelRequest> allRequests =
                travelRepository.findByEmpId(employee.getEmpId());

        LocalDateTime now = LocalDateTime.now();

        List<TravelRequest> requests = new ArrayList<>();

        for (TravelRequest request : allRequests) {

            if (!request.isPayrollProcessed()) {
                requests.add(request);
                continue;
            }

            if (request.getPayrollProcessedAt() == null) {
                requests.add(request);
                continue;
            }

            LocalDateTime hideAfter =
                    request.getPayrollProcessedAt().plusDays(2);

            if (now.isBefore(hideAfter)) {
                requests.add(request);
            }
        }

        return ResponseEntity.ok(requests);
    }
    @GetMapping("/my-requests")
    public String myTravelRequests(
            Principal principal,
            Model model) {

        String email = principal.getName();

        Employee employee =
                employeeRepository.findByEmail(email);

        List<TravelRequest> allRequests =
                travelRepository.findByEmpId(employee.getEmpId());

        LocalDateTime now = LocalDateTime.now();

        List<TravelRequest> requests = new ArrayList<>();

        for (TravelRequest request : allRequests) {

            if (!request.isPayrollProcessed()) {
                requests.add(request);
                continue;
            }

            if (request.getPayrollProcessedAt() == null) {
                requests.add(request);
                continue;
            }

            LocalDateTime hideAfter =
                    request.getPayrollProcessedAt().plusDays(2);

            if (now.isBefore(hideAfter)) {
                requests.add(request);
            }
        }

        model.addAttribute("requests", requests);

        return "my-travel-requests";
    }

    // ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¦ Manager View Requests
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

    // ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¦ Manager Approve
    @GetMapping("/manager-approve/{id}")
    @PreAuthorize("hasAuthority('WRITE_EMPLOYEE')")
    public String managerApprove(
            @PathVariable Long id,
            Principal principal) {

        TravelRequest request =
                travelRepository.findById(id)
                        .orElse(null);

        if(request != null){

            request.setStatus("MANAGER_APPROVED");

            travelRepository.save(request);

            // Audit
            TravelAudit audit = new TravelAudit();
            audit.setTravelRequestId(request.getId());
            Employee actingManager = principal == null ? null : employeeRepository.findByEmail(principal.getName());
            audit.setPerformedByEmployeeId(actingManager == null ? request.getEmpId() : actingManager.getEmpId());
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
            Employee employee = employeeRepository.findById(request.getEmpId()).orElse(null);
            if (employee != null) {
                String body = "<p>Your travel request to " + request.getDestination() + " has been approved by your manager.</p>";
                emailService.sendMail(employee.getEmail(), "Travel Request Manager Approved", body);
            }
        }

        return "redirect:/travel/manager";
    }

    // ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¦ Manager Reject
    @GetMapping("/manager-reject/{id}")
    @PreAuthorize("hasAuthority('WRITE_EMPLOYEE')")
    public String managerReject(
            @PathVariable Long id,
            Principal principal) {

        TravelRequest request =
                travelRepository.findById(id)
                        .orElse(null);

        if(request != null){

            request.setStatus("REJECTED");

            travelRepository.save(request);

            TravelAudit audit = new TravelAudit();
            audit.setTravelRequestId(request.getId());
            Employee actingManager = principal == null ? null : employeeRepository.findByEmail(principal.getName());
            audit.setPerformedByEmployeeId(actingManager == null ? request.getEmpId() : actingManager.getEmpId());
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

    // ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¦ HR/Admin View
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('READ_EMPLOYEE')")
    public String adminTravelRequests(Model model) {

        List<TravelRequest> requests = travelRepository.findByStatus("MANAGER_APPROVED");

        model.addAttribute(
                "requests",
                requests);

        return "admin-travel-requests";
    }

    // ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¦ HR Final Approval
    @GetMapping("/approve/{id}")
    @PreAuthorize("hasAuthority('WRITE_EMPLOYEE')")
    public String approveTravel(
            @PathVariable Long id,
            Principal principal) {

        TravelRequest request =
                travelRepository.findById(id)
                        .orElse(null);

        if (request != null) {
            request.setStatus("ADMIN_APPROVED");

            travelRepository.save(request);

            TravelAudit audit = new TravelAudit();
            audit.setTravelRequestId(request.getId());
            Employee actingAdmin = principal == null ? null : employeeRepository.findByEmail(principal.getName());
            audit.setPerformedByEmployeeId(actingAdmin == null ? request.getEmpId() : actingAdmin.getEmpId());
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

    // ÃƒÆ’Ã†â€™Ãƒâ€ Ã¢â‚¬â„¢ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã†â€™ÃƒÂ¢Ã¢â€šÂ¬Ã‚Â¦ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â‚¬Å¡Ã‚Â¬Ãƒâ€¦Ã¢â‚¬Å“ÃƒÆ’Ã†â€™Ãƒâ€šÃ‚Â¢ÃƒÆ’Ã‚Â¢ÃƒÂ¢Ã¢â€šÂ¬Ã…Â¡Ãƒâ€šÃ‚Â¬ÃƒÆ’Ã¢â‚¬Å¡Ãƒâ€šÃ‚Â¦ HR Reject
    @GetMapping("/reject/{id}")
    @PreAuthorize("hasAuthority('WRITE_EMPLOYEE')")
    public String rejectTravel(
            @PathVariable Long id,
            Principal principal) {

        TravelRequest request =
                travelRepository.findById(id)
                        .orElse(null);
        if (request != null) {

            request.setStatus("REJECTED");

            travelRepository.save(request);

            TravelAudit audit = new TravelAudit();
            audit.setTravelRequestId(request.getId());
            Employee actingAdmin = principal == null ? null : employeeRepository.findByEmail(principal.getName());
            audit.setPerformedByEmployeeId(actingAdmin == null ? request.getEmpId() : actingAdmin.getEmpId());
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


