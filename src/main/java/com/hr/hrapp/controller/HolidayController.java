package com.hr.hrapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.hr.hrapp.entity.Holiday;
import com.hr.hrapp.service.HolidayService;

@Controller
@RequestMapping("/admin")
public class HolidayController {

    @Autowired
    private HolidayService holidayService;

    @GetMapping("/holidays")
    public String holidaysPage(Model model) {

        model.addAttribute(
                "holiday",
                new Holiday());

        model.addAttribute(
                "holidays",
                holidayService.getAllHolidays());

        return "holiday-management";
    }

    @PostMapping("/save-holiday")
    public String saveHoliday(
            @ModelAttribute Holiday holiday) {

        holidayService.saveHoliday(
                holiday);

        return "redirect:/admin/holidays";
    }

    @GetMapping("/delete-holiday/{id}")
    public String deleteHoliday(
            @PathVariable Long id) {

        holidayService.deleteHoliday(id);

        return "redirect:/admin/holidays";
    }
}