package com.hr.hrapp.controller;

import java.security.Principal;
import jakarta.servlet.http.HttpSession;

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
import com.hr.hrapp.service.MfaService;
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
    private MfaService mfaService;

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
	    if(userRepository.findByUsername(username).isPresent()){
	        return "redirect:/register?error";
	    }

	    User user = new User();
	    user.setUsername(username);
	    user.setPassword(encoder.encode(password));
	    user.setRole("USER");
	    user.setForcePasswordChange(false);
	    user.setMfaEnabled(false);

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
	        Model model,
	        HttpSession session) {

	    Employee emp =
	            employeeRepository.findByEmail(email);

	    if (emp == null) {
	        model.addAttribute("error", "Email not found");
	        return "forgot-password";
	    }

	    User user =
	            userRepository.findByUsername(email)
	                    .orElse(null);

	    if (user == null) {
	        model.addAttribute("error", "User account not found");
	        return "forgot-password";
	    }

	    // TOTP must already be configured for secure password recovery
	    if (!user.isMfaEnabled()
	            || user.getTotpSecret() == null
	            || user.getTotpSecret().isBlank()) {

	        model.addAttribute(
	                "error",
	                "Authenticator setup is required for password recovery. Please contact HR/Admin."
	        );

	        return "forgot-password";
	    }

	    // Store only the account identity temporarily.
	    // Do NOT store password or TOTP code in session.
	    session.setAttribute("PASSWORD_RESET_EMAIL", email);

	    return "forgot-password-totp";
	}
	@PostMapping("/forgot-password/verify-totp")
	public String verifyForgotPasswordTotp(
	        @RequestParam String code,
	        HttpSession session,
	        Model model) {

	    try {

	        String email =
	                (String) session.getAttribute("PASSWORD_RESET_EMAIL");

	        // Reset flow must have been started first
	        if (email == null || email.isBlank()) {
	            return "redirect:/forgot-password?error";
	        }

	        User user =
	                userRepository.findByUsername(email)
	                        .orElse(null);

	        if (user == null
	                || !user.isMfaEnabled()
	                || user.getTotpSecret() == null
	                || user.getTotpSecret().isBlank()) {

	            session.removeAttribute("PASSWORD_RESET_EMAIL");

	            model.addAttribute(
	                    "error",
	                    "Authenticator verification is not available."
	            );

	            return "forgot-password";
	        }

	        int totpCode;

	        try {
	            totpCode = Integer.parseInt(code);
	        } catch (NumberFormatException e) {
	            model.addAttribute(
	                    "error",
	                    "Enter a valid 6-digit Authenticator code."
	            );
	            return "forgot-password-totp";
	        }

	        if (!mfaService.verifyCode(user.getTotpSecret(), totpCode)) {

	            model.addAttribute(
	                    "error",
	                    "Invalid Authenticator code."
	            );

	            return "forgot-password-totp";
	        }

	        // TOTP successfully verified
	        session.setAttribute(
	                "PASSWORD_RESET_TOTP_VERIFIED",
	                true
	        );

	        return "reset-password";

	    } catch (Exception e) {

	        logger.error(
	                "Forgot password TOTP verification failed",
	                e
	        );

	        model.addAttribute(
	                "error",
	                "Unable to verify Authenticator code."
	        );

	        return "forgot-password-totp";
	    }
	}
	@PostMapping("/reset-password")
	public String resetPassword(
	        @RequestParam String password,
	        HttpSession session,
	        Model model) {

	    // TOTP verification is mandatory
	    Boolean totpVerified =
	            (Boolean) session.getAttribute(
	                    "PASSWORD_RESET_TOTP_VERIFIED");

	    String email =
	            (String) session.getAttribute(
	                    "PASSWORD_RESET_EMAIL");

	    if (!Boolean.TRUE.equals(totpVerified)
	            || email == null
	            || email.isBlank()) {

	        session.removeAttribute("PASSWORD_RESET_EMAIL");
	        session.removeAttribute("PASSWORD_RESET_TOTP_VERIFIED");

	        return "redirect:/forgot-password?error";
	    }

	    // Basic password validation
	    if (password == null || password.length() < 8) {

	        model.addAttribute(
	                "error",
	                "Password must be at least 8 characters."
	        );

	        return "reset-password";
	    }

	    User user =
	            userRepository.findByUsername(email)
	                    .orElse(null);

	    if (user == null) {

	        session.removeAttribute("PASSWORD_RESET_EMAIL");
	        session.removeAttribute("PASSWORD_RESET_TOTP_VERIFIED");

	        return "redirect:/forgot-password?error";
	    }

	    user.setPassword(
	            encoder.encode(password)
	    );

	    user.setForcePasswordChange(false);

	    userRepository.save(user);

	    // One-time reset session cleanup
	    session.removeAttribute("PASSWORD_RESET_EMAIL");
	    session.removeAttribute("PASSWORD_RESET_TOTP_VERIFIED");

	    return "redirect:/login?resetSuccess";
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

    String username = authentication.getName();

    User user = userRepository.findByUsername(username)
            .orElse(null);

    // Force employee to change temporary password
    if (user != null && user.isForcePasswordChange()) {
        return "redirect:/change-password";
    }

        if (user != null && user.isMfaEnabled() && user.getTotpSecret() != null) {
            return "redirect:/mfa";
        }

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
	@GetMapping("/mfa")
	public String mfaPage(Principal principal, Model model) {

	    String username = principal.getName();

	    User user = userRepository.findByUsername(username)
	            .orElse(null);

	    model.addAttribute("username", username);

	    boolean setupRequired = user == null
	            || !user.isMfaEnabled()
	            || user.getTotpSecret() == null
	            || user.getTotpSecret().isBlank();

	    model.addAttribute("setupRequired", setupRequired);

	    return "mfa";
	}
    @PostMapping("/mfa/verify")
    public String verifyMfa(@RequestParam String code, Principal principal, HttpSession session) {
        try {
            int otp = Integer.parseInt(code);

            User user = userRepository.findByUsername(principal.getName())
                    .orElse(null);

            if (user == null || !user.isMfaEnabled() || user.getTotpSecret() == null) {
                return "redirect:/login?error";
            }

            if (!mfaService.verifyCode(user.getTotpSecret(), otp)) {
                return "redirect:/mfa?error";
            }

        session.setAttribute("MFA_VERIFIED", true);
            String role = user.getRole();

            if ("ADMIN".equals(role)) {
                return "redirect:/admin/dashboard";
            } else {
                return "redirect:/user/dashboard";
            }

        } catch (NumberFormatException e) {
            return "redirect:/mfa?error";
        }
    }

    @GetMapping("/change-password")
    public String showChangePasswordPage(HttpSession session) {

        session.setAttribute("CHANGE_PASSWORD_TOTP_REQUIRED", true);

        return "change-password-totp";
    }
    @PostMapping("/change-password/verify-totp")
    public String verifyChangePasswordTotp(
            @RequestParam String code,
            Principal principal,
            HttpSession session,
            Model model) {

        try {

            // TOTP verification flow must be started first
            Boolean required =
                    (Boolean) session.getAttribute(
                            "CHANGE_PASSWORD_TOTP_REQUIRED");

            if (!Boolean.TRUE.equals(required)) {
                return "redirect:/change-password";
            }

            if (principal == null) {
                return "redirect:/login";
            }

            User user =
                    userRepository.findByUsername(principal.getName())
                            .orElse(null);

            if (user == null
                    || !user.isMfaEnabled()
                    || user.getTotpSecret() == null
                    || user.getTotpSecret().isBlank()) {

                session.removeAttribute(
                        "CHANGE_PASSWORD_TOTP_REQUIRED");

                model.addAttribute(
                        "error",
                        "Authenticator verification is not available.");

                return "change-password-totp";
            }

            int totpCode;

            try {
                totpCode = Integer.parseInt(code);
            } catch (NumberFormatException e) {

                model.addAttribute(
                        "error",
                        "Enter a valid 6-digit Authenticator code.");

                return "change-password-totp";
            }

            if (!mfaService.verifyCode(
                    user.getTotpSecret(),
                    totpCode)) {

                model.addAttribute(
                        "error",
                        "Invalid Authenticator code.");

                return "change-password-totp";
            }

            // TOTP successfully verified
            session.setAttribute(
                    "CHANGE_PASSWORD_TOTP_VERIFIED",
                    true);

            session.removeAttribute(
                    "CHANGE_PASSWORD_TOTP_REQUIRED");

            return "change-password";

        } catch (Exception e) {

            logger.error(
                    "Change password TOTP verification failed",
                    e);

            model.addAttribute(
                    "error",
                    "Unable to verify Authenticator code.");

            return "change-password-totp";
        }
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            HttpSession session,
            Model model) {

        // Authenticator verification is mandatory
        Boolean totpVerified =
                (Boolean) session.getAttribute(
                        "CHANGE_PASSWORD_TOTP_VERIFIED");

        if (!Boolean.TRUE.equals(totpVerified)) {
            return "redirect:/change-password";
        }

        // User must be authenticated
        if (authentication == null
                || !authentication.isAuthenticated()) {
            session.removeAttribute(
                    "CHANGE_PASSWORD_TOTP_VERIFIED");

            return "redirect:/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute(
                    "error",
                    "Passwords do not match");

            return "change-password";
        }

        if (newPassword.length() < 8) {
            model.addAttribute(
                    "error",
                    "Password must be at least 8 characters");

            return "change-password";
        }

        String username = authentication.getName();

        User user =
                userRepository.findByUsername(username)
                        .orElse(null);

        if (user == null) {
            session.removeAttribute(
                    "CHANGE_PASSWORD_TOTP_VERIFIED");

            model.addAttribute(
                    "error",
                    "User not found");

            return "change-password";
        }

        // Existing password update logic preserved
        user.setPassword(
                encoder.encode(newPassword));

        user.setForcePasswordChange(false);

        userRepository.save(user);

        // One-time TOTP verification cleanup
        session.removeAttribute(
                "CHANGE_PASSWORD_TOTP_VERIFIED");

        return "redirect:/user/dashboard";
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
