package com.hr.hrapp.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.Salary;
import com.hr.hrapp.entity.Timesheet;
import com.hr.hrapp.repository.TimesheetRepository;

@Service
public class FinancialService {
	
	@Autowired
    private TimesheetRepository timesheetRepository;
	
	public List<Map<String, Object>> processSalary(List<Salary> list) {

        List<Map<String, Object>> result = new ArrayList<>();

        double prev = 0;

        for (Salary s : list) {

            Map<String, Object> map = new HashMap<>();

            double hike = s.getBasicSalary() - prev;

            map.put("month", s.getMonth());
            map.put("basic", s.getBasicSalary());
            map.put("net", s.getNetSalary());
            map.put("hike", hike);

            prev = s.getBasicSalary();

            result.add(map);
        }

        return result;
    }
	 public double calculateSalary(Employee emp, int month, int year) {
		 
		 LocalDate start = LocalDate.of(year, month, 1);
		    LocalDate end = 
		    		start.withDayOfMonth(start.lengthOfMonth());

	        List<Timesheet> list =
	            timesheetRepository.findByEmployeeIdAndDateBetween(emp.getEmpId(), start, end);

	        long presentDays = list.stream()
	            .filter(t -> "PRESENT".equals(t.getStatus()))
	            .count();

	        double perDay = emp.getBasicSalary() / 30;

	        return presentDays * perDay;
	    }
	}
	

