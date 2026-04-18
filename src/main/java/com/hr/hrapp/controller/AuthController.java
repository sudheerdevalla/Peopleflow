package com.hr.hrapp.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.EmployeeAttendance;
import com.hr.hrapp.entity.User;
import com.hr.hrapp.repository.EmployeeAttendanceRepository;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.repository.UserRepository;
@Controller
public class AuthController {
	@Autowired
	private EmployeeRepository employeeRepository;
	@Autowired
	private EmployeeAttendanceRepository employeeAttendanceRepository;
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private BCryptPasswordEncoder encoder;
	
	@GetMapping("/register")
	public String showRegister() {
	    return "register";
	}
	@PostMapping("/register")
	public String registerUser(@RequestParam String username,
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
	    emp.setName(username);
	    emp.setEmail(username);
	    emp.setDepartment("Not Assigned");
	    emp.setBasicSalary(0);
	    emp.setStatus("Active");
	    emp.setRole("USER");
	     employeeRepository.save(emp);

	    return "redirect:/login";
	}
	@GetMapping("/login")
    public String loginPage() {
        return "login";
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
	    model.addAttribute("employees", employeeRepository.findAll());
	    model.addAttribute("empCount", employeeRepository.count());
	    return "admin-dashboard";
	}
	// Show form
	@GetMapping("/admin/add-employee")
	public String showAddForm(Model model) {
	    model.addAttribute("employee", new Employee());
	    return "add-employee";
	}

	// Save employee
	@PostMapping("/admin/save-employee")
	public String saveEmployee(@ModelAttribute Employee employee) {
	    employeeRepository.save(employee);
	    return "redirect:/admin/employees";
	}

}
