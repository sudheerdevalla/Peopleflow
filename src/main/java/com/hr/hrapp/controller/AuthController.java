package com.hr.hrapp.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.EmployeeAttendance;
import com.hr.hrapp.entity.User;
import com.hr.hrapp.repository.CandidateRepository;
import com.hr.hrapp.repository.CompanyUpdateRepository;
import com.hr.hrapp.repository.EmployeeAttendanceRepository;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.repository.LeaveRepository;
import com.hr.hrapp.repository.TravelRequestRepository;
import com.hr.hrapp.repository.UserRepository;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;


@Controller
public class AuthController {
	private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
	@Autowired
	private EmployeeRepository employeeRepository;
	@Autowired
	private EmployeeAttendanceRepository employeeAttendanceRepository;
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder encoder;
	
	@Autowired
	private LeaveRepository leaveRepository;
	
	@Autowired
	private TravelRequestRepository travelRequestRepository;
	
    @Autowired
    private com.hr.hrapp.service.EmailService emailService;

	@Autowired
	private JavaMailSender mailSender;
	
	@Autowired
	private CompanyUpdateRepository companyUpdateRepository;
	
	@Autowired
	private CandidateRepository candidateRepository;
	
	private String generatedOtp;
	private String otpEmail;
	private java.time.LocalDateTime otpTime;
	
	@GetMapping("/register")
	public String showRegister() {
	    return "register";
	}
	@PostMapping("/register")
	public String registerUser(@RequestParam String name,
			                   @RequestParam String username,
	                           @RequestParam String password) {

	    // check if user exists
	    if(userRepository.findByUsername(username) != null){
	        return "redirect:/register?error";
	    }

	    User user = new User();
	    user.setUsername(username);
	    user.setPassword(encoder.encode(password));
	    user.setRole("USER");

	    userRepository.save(user);
	    
	    Employee emp = new Employee();
	    emp.setName(name);
	    emp.setEmail(username);
	    emp.setDepartment("Not Assigned");
	    emp.setBasicSalary(0);
	    emp.setStatus("Active");
	    emp.setRole("USER");
	     employeeRepository.save(emp);

	    // Send welcome email to newly registered employee
	    try {
	        String body = "<p>Dear " + name + ",</p>"
	                + "<p>Welcome to Renwion Clean Enviro Solutions Private Limited. Your account has been created.</p>"
	                + "<p>Regards,<br/>HR Team</p>";

	        emailService.sendMail(
	                username,
	                "Welcome to Renwion Clean Enviro Solutions",
	                body
	        );
		} catch (Exception e) {
			logger.error("Failed to send welcome email to {}", username, e);
		}

	    return "redirect:/login";
	}
	@GetMapping("/login")
    public String loginPage() {
        return "login";
    }
	@GetMapping("/forgot-password")
	public String forgotPasswordPage() {
	    return "forgot-password";
	}

	@PostMapping("/forgot-password")
	public String forgotPassword(
	        @RequestParam String email,
	        Model model) {

	    Employee emp =
	            employeeRepository
	                    .findByEmail(email);

	    if(emp == null) {

	        model.addAttribute(
	                "error",
	                "Email not found");

	        return "forgot-password";
	    }

	    generatedOtp =
	            String.valueOf(
	                    100000 +
	                    new java.util.Random()
	                    .nextInt(900000));

	    otpEmail = email;
	    
	    otpTime =
	            java.time.LocalDateTime.now();

	    try {
	        MimeMessage message = mailSender.createMimeMessage();
	        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
	        
	        helper.setFrom("connect@renwion.in");
	        helper.setTo(email);
	        helper.setSubject("PeopleFlow Password Reset OTP");
	        helper.setText("Your OTP is: " + generatedOtp, true);
	        
	        mailSender.send(message);
		} catch (Exception e) {
			logger.error("Failed to send OTP to {}", email, e);
			model.addAttribute("error", "Failed to send OTP email");
			return "forgot-password";
		}

	    model.addAttribute(
	            "email",
	            email);

	    return "verify-otp";
	}
	@PostMapping("/reset-password")
	public String resetPassword(
	        @RequestParam String email,
	        @RequestParam String password) {

		User user = userRepository
		        .findByUsername(email)
		        .orElse(null);

		if (user != null) {

		    user.setPassword(
		            encoder.encode(password));

		    userRepository.save(user);

		    generatedOtp = null;
		    otpEmail = null;

		    return "redirect:/login?resetSuccess";
		}

		return "redirect:/forgot-password?error";
	}
	@PostMapping("/verify-otp")
	public String verifyOtp(
	        @RequestParam String otp,
	        Model model) {

	    if (otpTime.plusMinutes(5)
	            .isBefore(
	                    java.time.LocalDateTime.now())) {

	        model.addAttribute(
	                "error",
	                "OTP Expired");

	        return "verify-otp";
	    }

	    if(otp.equals(generatedOtp)) {

	        model.addAttribute(
	                "email",
	                otpEmail);

	        return "reset-password";
	    }

	    model.addAttribute(
	            "error",
	            "Invalid OTP");

	    return "verify-otp";
	}
	
	@GetMapping("/default")
	public String loginSuccess(Authentication authentication) {

	    String role = authentication.getAuthorities()
	                                .iterator()
	                                .next()
	                                .getAuthority();

	    if (role.equals("ROLE_ADMIN")) {
	        return "redirect:/admin/dashboard";
	    } else {
	        return "redirect:/user/dashboard";
	    }
	}
	

	@GetMapping("/admin/dashboard")
	public String admindashboard(Model model) {

	    long totalEmployees =
	            employeeRepository.count();

	    long activeEmployees =
	            employeeRepository.countByStatus("Active");

	    long pendingLeaves =
	            leaveRepository.countByStatus("PENDING");
	    long approvedLeaves =
	            leaveRepository.countByStatus("APPROVED");

	    long rejectedLeaves =
	            leaveRepository.countByStatus("REJECTED");

		long travelCount = travelRequestRepository.findByStatus("REQUESTED").size();
	    long departmentCount =
	            employeeRepository.findAll()
	            .stream()
	            .map(Employee::getDepartment)
	            .filter(d -> d != null && !d.isBlank())
	            .distinct()
	            .count();
	    long itCount =
	            employeeRepository.countByDepartment("IT");

	    long hrCount =
	            employeeRepository.countByDepartment("HR");

	    long financeCount =
	            employeeRepository.countByDepartment("Finance");

	    long adminCount =
	            employeeRepository.countByDepartment("Admin");
		// pendingTravel includes newly REQUESTED and those approved by manager but pending admin
		long pendingTravel = travelRequestRepository.findByStatus("REQUESTED").size()
				+ travelRequestRepository.findByStatus("MANAGER_APPROVED").size();

		long approvedTravel = travelRequestRepository.findByStatus("ADMIN_APPROVED").size();

		long rejectedTravel = travelRequestRepository.findByStatus("REJECTED").size();
		
		long candidateCount =
		        candidateRepository.count();

		long appliedCandidates =
		        candidateRepository.countByStatus("APPLIED");

		long shortlistedCandidates =
		        candidateRepository.countByStatus("SHORTLISTED");

		long selectedCandidates =
		        candidateRepository.countByStatus("SELECTED");

		long rejectedCandidates =
		        candidateRepository.countByStatus("REJECTED");
	   

	    model.addAttribute(
	            "approvedLeaves",
	            approvedLeaves);

	    model.addAttribute(
	            "rejectedLeaves",
	            rejectedLeaves);

	    model.addAttribute("employees",
	            employeeRepository.findAll());

	    model.addAttribute("empCount",
	            totalEmployees);

	    model.addAttribute("activeEmployees",
	            activeEmployees);

	    model.addAttribute("pendingLeaves",
	            pendingLeaves);

	    model.addAttribute("presentToday",
	            activeEmployees);

	    model.addAttribute("travelCount",
	            travelCount);

	    model.addAttribute("departmentCount",
	            departmentCount);
	    
	    model.addAttribute("itCount", itCount);

	    model.addAttribute("hrCount", hrCount);

	    model.addAttribute("financeCount", financeCount);

	    model.addAttribute("adminCount", adminCount);
	    model.addAttribute("pendingTravel", pendingTravel);
	    
	    model.addAttribute("approvedTravel", approvedTravel);
	    
	    model.addAttribute("rejectedTravel", rejectedTravel);
	    
	    model.addAttribute(
	            "candidateCount",
	            candidateCount);

	    model.addAttribute(
	            "appliedCandidates",
	            appliedCandidates);

	    model.addAttribute(
	            "shortlistedCandidates",
	            shortlistedCandidates);

	    model.addAttribute(
	            "selectedCandidates",
	            selectedCandidates);

	    model.addAttribute(
	            "rejectedCandidates",
	            rejectedCandidates);

	    return "admin-dashboard";
	}
	// Show form
	@GetMapping("/admin/add-employee")
	public String showAddForm(Model model) {

	    // ✅ Empty employee object
	    model.addAttribute("employee", new Employee());

	    // ✅ Manager dropdown data
	    model.addAttribute("managers",
	            employeeRepository.findAll());

	    return "add-employee";
	}
	

	// Save employee
	@PostMapping("/admin/save-employee")
	public String saveEmployee(@ModelAttribute Employee employee) {
	    employeeRepository.save(employee);
	    return "redirect:/admin/employees";
	}
	@GetMapping("/user/my-tree")
	public String myTree(
	        Principal principal,
	        Model model) {

	    String email = principal.getName();

	    Employee employee =
	            employeeRepository.findByEmail(email);

	    model.addAttribute(
	            "employee",
	            employee);

	    return "employee-tree";
	}
	@GetMapping("/admin/updates/delete/{id}")
	public String deleteUpdate(@PathVariable Long id){

	    companyUpdateRepository.deleteById(id);

	    return "redirect:/admin/updates";
	}
	

}
