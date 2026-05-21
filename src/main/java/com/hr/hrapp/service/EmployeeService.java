package com.hr.hrapp.service;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.Period;

@Service
public class EmployeeService {

    public String calculateExperience(LocalDate joiningDate) {

        if (joiningDate == null) return "0 Years";

        Period p = Period.between(joiningDate, LocalDate.now());

        return p.getYears() + " Years " + p.getMonths() + " Months";
    }
}