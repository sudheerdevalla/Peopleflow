package com.hr.hrapp.service;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.repository.EmployeeRepository;



@Service
public class ExcelEmployeeService {
	
	@Autowired
	private EmployeeRepository employeeRepository;
    
	@Autowired
	private com.hr.hrapp.service.EmailService emailService;

	public void importEmployees(MultipartFile file) {

	    try (Workbook workbook =
	            new XSSFWorkbook(file.getInputStream())) {

	        Sheet sheet = workbook.getSheetAt(0);

	        boolean firstRow = true;

	        for (Row row : sheet) {

	            if (firstRow) {
	                firstRow = false;
	                continue;
	            }

	            Employee emp = new Employee();

	            emp.setName(
	                row.getCell(0).getStringCellValue()
	            );

	            emp.setEmail(
	                row.getCell(1).getStringCellValue()
	            );

	            emp.setBasicSalary(
	                row.getCell(2).getNumericCellValue()
	            );
	            
	            emp.setStatus("Active");
	            emp.setRole("USER");

							employeeRepository.save(emp);

							// Send welcome email for imported employee
							try {
								String body = "<p>Dear " + emp.getName() + ",</p>"
										+ "<p>Welcome to Renwion Clean Enviro Solutions Private Limited. Your account has been created.</p>"
										+ "<p>Regards,<br/>HR Team</p>";

								emailService.sendMail(
										emp.getEmail(),
										"Welcome to Renwion Clean Enviro Solutions",
										body
								);
							} catch (Exception e) {
								e.printStackTrace();
							}
	        }

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}