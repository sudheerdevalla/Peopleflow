package com.hr.hrapp.controller;


import com.hr.hrapp.entity.CompanyUpdate;
import com.hr.hrapp.repository.CompanyUpdateRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/updates")
public class AdminCompanyUpdateController {

@Autowired
private CompanyUpdateRepository companyUpdateRepository;

@GetMapping
public String updatesPage(Model model) {
    model.addAttribute("update", new CompanyUpdate());
    model.addAttribute("updates",
            companyUpdateRepository.findAll());
    return "admin-updates";
}

@PostMapping("/save")
public String saveUpdate(@ModelAttribute CompanyUpdate update) {
    companyUpdateRepository.save(update);
    return "redirect:/admin/updates";
}


}

