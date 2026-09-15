package com.hr.hrapp.payroll.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.hr.hrapp.payroll.entity.Payroll;
import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.repository.EmployeeRepository;
import com.hr.hrapp.payroll.repository.PayrollRepository;
import com.hr.hrapp.util.PayrollMonthUtil;

import jakarta.mail.internet.MimeMessage;

@Service
public class CEOReportService {

    @Autowired
    private PayrollRepository payrollRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.admin.email}")
    private String adminEmail;

    public void sendCEOReport() {

        try {

        	LocalDate previousMonth =
        	        LocalDate.now().minusMonths(1);

            String currentMonth = PayrollMonthUtil.format(previousMonth);

            List<Payroll> payrolls =
                    payrollRepository.findByMonthAndStatusOrderByEmployeeIdAsc(
                            currentMonth,
                            "FINALIZED");

            if (payrolls.isEmpty()) {
                payrolls = payrollRepository.findByMonth(currentMonth);
            }

            // =========================
            // EXCEL CREATE
            // =========================

              XSSFWorkbook workbook = buildWorkbook(payrolls, currentMonth);

            // =========================
            // SAVE FILE
            // =========================

            File file =
                    File.createTempFile(
                            "CEO-Payroll-Report",
                            ".xlsx");

            FileOutputStream fos =
                    new FileOutputStream(file);

            workbook.write(fos);

            fos.close();

            workbook.close();

            // =========================
            // SEND MAIL
            // =========================

            MimeMessage message =
                    mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message,
                            true);

            helper.setFrom("connect@renwion.in");
            helper.setTo(adminEmail);

            helper.setSubject(
                    "PeopleFlow Consolidated Payroll Report");

            helper.setText(
                    "Attached is the consolidated payroll report.");

            helper.addAttachment(
                    "PayrollReport.xlsx",
                    file);

            mailSender.send(message);

            System.out.println(
                    "CEO REPORT SENT");

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

      public ByteArrayInputStream generateConsolidatedSalarySheet(java.time.YearMonth month) {
          String monthLabel = PayrollMonthUtil.format(month);
          List<Payroll> payrolls = payrollRepository.findByMonthAndStatusOrderByEmployeeIdAsc(monthLabel, "FINALIZED");
          if (payrolls.isEmpty()) {
              throw new IllegalStateException("No finalized payroll found for " + monthLabel);
          }

          try (XSSFWorkbook workbook = buildWorkbook(payrolls, monthLabel);
               ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
              workbook.write(outputStream);
              return new ByteArrayInputStream(outputStream.toByteArray());
          } catch (Exception ex) {
              throw new RuntimeException("Failed to generate consolidated salary sheet", ex);
          }
      }

      private XSSFWorkbook buildWorkbook(List<Payroll> payrolls, String monthLabel) {

    	    XSSFWorkbook workbook = new XSSFWorkbook();
    	    XSSFSheet sheet = workbook.createSheet("CEO Payroll Report");
    	    
    	 // =========================
    	 // REPORT TITLE
    	 // =========================

    	 Row titleRow = sheet.createRow(0);

    	 titleRow.createCell(0).setCellValue(
    	         "PEOPLEFLOW - CONSOLIDATED PAYROLL REPORT");

    	 titleRow.createCell(1).setCellValue(
    	         "Payroll Month: " + monthLabel);

    	 org.apache.poi.ss.usermodel.Font titleFont =
    	         workbook.createFont();

    	 titleFont.setBold(true);
    	 titleFont.setFontHeightInPoints((short) 14);

    	 org.apache.poi.ss.usermodel.CellStyle titleStyle =
    	         workbook.createCellStyle();

    	 titleStyle.setFont(titleFont);

    	 titleRow.getCell(0).setCellStyle(titleStyle);
    	 titleRow.getCell(1).setCellStyle(titleStyle);


    	 // =========================
    	 // REPORT INFORMATION
    	 // =========================

    	 Row infoRow = sheet.createRow(1);

    	 infoRow.createCell(0).setCellValue(
    	         "Confidential - Management Use Only");

    	 infoRow.createCell(1).setCellValue(
    	         "Generated by PeopleFlow");
    	 // =========================
    	 // CEO REPORT TITLE
    	 // =========================

    	 Row reportTitle = sheet.createRow(2);

    	 reportTitle.createCell(0).setCellValue(
    	         "PEOPLEFLOW - CONSOLIDATED PAYROLL REPORT");

    	 reportTitle.createCell(1).setCellValue(
    	         "Payroll Month: " + monthLabel);

    	 org.apache.poi.ss.usermodel.Font reportTitleFont =
    		        workbook.createFont();

    		reportTitleFont.setBold(true);
    		reportTitleFont.setFontHeightInPoints((short) 14);

    		org.apache.poi.ss.usermodel.CellStyle reportTitleStyle =
    		        workbook.createCellStyle();

    		reportTitleStyle.setFont(reportTitleFont);

    		reportTitle.getCell(0).setCellStyle(reportTitleStyle);
    		reportTitle.getCell(1).setCellStyle(reportTitleStyle);

    	    // =========================
    	    // HEADER
    	    // =========================

    	    String[] headers = {
    	        "Month",
    	        "Payroll ID",
    	        "Employee ID",
    	        "Employee Code",
    	        "Employee Name",
    	        "Email",
    	        "Department",
    	        "Role",
    	        "Designation",
    	        "Location",
    	        "Employment Type",
    	        "Joining Date",
    	        "Confirmation Date",
    	        "Manager",

    	        "Annual CTC",
    	        "Monthly Gross",
    	        "HRA %",
    	        "Bonus %",
    	        "Basic Salary",
    	        "HRA",
    	        "Conveyance",
    	        "Telephone",
    	        "Internet",
    	        "Travel Allowance",
    	        "Special Allowance",
    	        "Approved Additions",
    	        "Gross Earnings",

    	        "Payable Days",
    	        "Working Days",

    	        "Employee PF",
    	        "Employee ESI",
    	        "Professional Tax",
    	        "TDS",
    	        "Health Insurance",
    	        "Advance Salary Recovery",
    	        "Total Deductions",
    	        "Net Salary",

    	        "Employer PF",
    	        "Employer EPS",
    	        "Employer ESI",
    	        "PF Admin",
    	        "EDLI",
    	        "Total Employer Contribution",
    	        "Total CTC",

    	        "PF Number",
    	        "UAN Number",
    	        "ESI Number",
    	        "ESI Applicable",
    	        "EPS Applicable",
    	        "Payment Mode",

    	        "Payroll Status",
    	        "Reconciliation Status",
    	        "Last Calculated At",
    	        "Finalized At",
    	        "Finalized By"
    	    };

    	    Row header = sheet.createRow(2);

    	    org.apache.poi.ss.usermodel.CellStyle headerStyle =
    	            workbook.createCellStyle();

    	    org.apache.poi.ss.usermodel.Font headerFont =
    	            workbook.createFont();

    	    headerFont.setBold(true);
    	    headerFont.setFontHeightInPoints((short) 11);

    	    headerStyle.setFont(headerFont);

    	    headerStyle.setFillForegroundColor(
    	            org.apache.poi.ss.usermodel.IndexedColors.GREY_25_PERCENT.getIndex());

    	    headerStyle.setFillPattern(
    	            org.apache.poi.ss.usermodel.FillPatternType.SOLID_FOREGROUND);

    	    headerStyle.setBorderBottom(
    	            org.apache.poi.ss.usermodel.BorderStyle.THIN);

    	    for (int i = 0; i < headers.length; i++) {
    	        header.createCell(i).setCellValue(headers[i]);
    	        header.getCell(i).setCellStyle(headerStyle);
    	    }

    	    // =========================
    	    // DATA
    	    // =========================

    	    int rowNum = 3;

    	    for (Payroll payroll : payrolls) {

    	        Employee employee =
    	                employeeRepository.findById(payroll.getEmployeeId())
    	                        .orElse(null);

    	        Row row = sheet.createRow(rowNum++);

    	        int col = 0;

    	        // Employee / Organisation
    	        row.createCell(col++).setCellValue(monthLabel);
    	        row.createCell(col++).setCellValue(
    	                payroll.getId() == null ? 0 : payroll.getId());
    	        row.createCell(col++).setCellValue(
    	                payroll.getEmployeeId() == null ? 0 : payroll.getEmployeeId());

    	        row.createCell(col++).setCellValue(
    	                employee != null && employee.getEmployeeCode() != null
    	                        ? employee.getEmployeeCode()
    	                        : "");

    	        row.createCell(col++).setCellValue(
    	                payroll.getEmployeeName() == null
    	                        ? ""
    	                        : payroll.getEmployeeName());

    	        row.createCell(col++).setCellValue(
    	                employee != null && employee.getEmail() != null
    	                        ? employee.getEmail()
    	                        : "");

    	        row.createCell(col++).setCellValue(
    	                employee != null && employee.getDepartment() != null
    	                        ? employee.getDepartment()
    	                        : "");

    	        row.createCell(col++).setCellValue(
    	                employee != null && employee.getRole() != null
    	                        ? employee.getRole()
    	                        : "");

    	        row.createCell(col++).setCellValue(
    	                employee != null && employee.getDesignation() != null
    	                        ? employee.getDesignation()
    	                        : "");

    	        row.createCell(col++).setCellValue(
    	                employee != null && employee.getLocation() != null
    	                        ? employee.getLocation()
    	                        : "");

    	        row.createCell(col++).setCellValue(
    	                employee != null && employee.getEmploymentType() != null
    	                        ? employee.getEmploymentType()
    	                        : "");

    	        row.createCell(col++).setCellValue(
    	                employee != null && employee.getJoiningDate() != null
    	                        ? employee.getJoiningDate().toString()
    	                        : "");

    	        row.createCell(col++).setCellValue(
    	                employee != null && employee.getConfirmationDate() != null
    	                        ? employee.getConfirmationDate().toString()
    	                        : "");

    	        String managerName = "";

    	        if (employee != null
    	                && employee.getManager() != null
    	                && employee.getManager().getName() != null) {

    	            managerName = employee.getManager().getName();
    	        }

    	        row.createCell(col++).setCellValue(managerName);

    	        // Compensation
    	        row.createCell(col++).setCellValue(
    	                employee != null && employee.getAnnualCtc() != null
    	                        ? employee.getAnnualCtc().doubleValue()
    	                        : 0.0);

    	        row.createCell(col++).setCellValue(
    	                employee != null && employee.getMonthlyGrossSalary() != null
    	                        ? employee.getMonthlyGrossSalary().doubleValue()
    	                        : 0.0);

    	        row.createCell(col++).setCellValue(
    	                employee != null && employee.getHraPercentage() != null
    	                        ? employee.getHraPercentage()
    	                        : 0.0);

    	        row.createCell(col++).setCellValue(
    	                employee != null && employee.getBonusPercentage() != null
    	                        ? employee.getBonusPercentage()
    	                        : 0.0);

    	        row.createCell(col++).setCellValue(payroll.getBasicSalary());
    	        row.createCell(col++).setCellValue(payroll.getHra());
    	        row.createCell(col++).setCellValue(payroll.getConveyance());
    	        row.createCell(col++).setCellValue(payroll.getTelephone());
    	        row.createCell(col++).setCellValue(payroll.getInternet());

    	        row.createCell(col++).setCellValue(
    	                payroll.getTravelAllowance() == null
    	                        ? 0.0
    	                        : payroll.getTravelAllowance());

    	        row.createCell(col++).setCellValue(
    	                payroll.getSpecialAllowance());

    	        row.createCell(col++).setCellValue(
    	                payroll.getApprovedAdditions());

    	        row.createCell(col++).setCellValue(
    	                payroll.getGrossEarning());

    	        // Attendance
    	        row.createCell(col++).setCellValue(
    	                payroll.getPayableDays());

    	        row.createCell(col++).setCellValue(
    	                payroll.getWorkingDays());

    	        // Employee deductions
    	        row.createCell(col++).setCellValue(
    	                payroll.getPf());

    	        row.createCell(col++).setCellValue(
    	                payroll.getEmployeeEsi());

    	        row.createCell(col++).setCellValue(
    	                payroll.getProfessionalTax());

    	        row.createCell(col++).setCellValue(
    	                payroll.getTds());

    	        row.createCell(col++).setCellValue(
    	                payroll.getGroupHealthInsurance());

    	        row.createCell(col++).setCellValue(
    	                payroll.getAdvanceSalaryRecovery());

    	        row.createCell(col++).setCellValue(
    	                payroll.getTotalDeductions());

    	        row.createCell(col++).setCellValue(
    	                payroll.getNetSalary());

    	        // Employer contribution
    	        row.createCell(col++).setCellValue(
    	                payroll.getEmployerPf());

    	        row.createCell(col++).setCellValue(
    	                payroll.getEmployerEps());

    	        row.createCell(col++).setCellValue(
    	                payroll.getEmployerEsi());

    	        row.createCell(col++).setCellValue(
    	                payroll.getEmployerPfAdmin());

    	        row.createCell(col++).setCellValue(
    	                payroll.getEdli());

    	        row.createCell(col++).setCellValue(
    	                payroll.getTotalEmployerContribution());

    	        row.createCell(col++).setCellValue(
    	                payroll.getCtc());

    	        // Statutory / payment details
    	        row.createCell(col++).setCellValue(
    	                employee != null && employee.getPfNumber() != null
    	                        ? employee.getPfNumber()
    	                        : "");

    	        row.createCell(col++).setCellValue(
    	                employee != null && employee.getUanNumber() != null
    	                        ? employee.getUanNumber()
    	                        : "");

    	        row.createCell(col++).setCellValue(
    	                employee != null && employee.getEsiNumber() != null
    	                        ? employee.getEsiNumber()
    	                        : "");

    	        row.createCell(col++).setCellValue(
    	                employee != null && employee.isEsiApplicable()
    	                        ? "Yes"
    	                        : "No");

    	        row.createCell(col++).setCellValue(
    	                employee != null && employee.isEpsApplicable()
    	                        ? "Yes"
    	                        : "No");

    	        row.createCell(col++).setCellValue(
    	                employee != null && employee.getPaymentMode() != null
    	                        ? employee.getPaymentMode()
    	                        : "");

    	        // Payroll control
    	        row.createCell(col++).setCellValue(
    	                payroll.getStatus() == null
    	                        ? ""
    	                        : payroll.getStatus());

    	        row.createCell(col++).setCellValue(
    	                payroll.getReconciliationStatus() == null
    	                        ? ""
    	                        : payroll.getReconciliationStatus());

    	        row.createCell(col++).setCellValue(
    	                payroll.getLastCalculatedAt() == null
    	                        ? ""
    	                        : payroll.getLastCalculatedAt().toString());

    	        row.createCell(col++).setCellValue(
    	                payroll.getFinalizedAt() == null
    	                        ? ""
    	                        : payroll.getFinalizedAt().toString());

    	        row.createCell(col++).setCellValue(
    	                payroll.getFinalizedBy() == null
    	                        ? ""
    	                        : payroll.getFinalizedBy());
    	    }

    	    // =========================
    	    // AUTO SIZE
    	    // =========================

    	    for (int i = 0; i < headers.length; i++) {
    	        sheet.autoSizeColumn(i);

    	        // Prevent extremely wide columns
    	        if (sheet.getColumnWidth(i) > 15000) {
    	            sheet.setColumnWidth(i, 15000);
    	        }
    	    }

    	    // Freeze header row
    	    sheet.createFreezePane(0, 1);

    	    // Filter
    	    sheet.setAutoFilter(
    	            new CellRangeAddress(
    	                    2,
    	                    2 + payrolls.size(),
    	                    0,
    	                    headers.length - 1
    	            )
    	    );
    	    
    	 // =========================
    	 // CEO SUMMARY
    	 // =========================

    	 XSSFSheet summarySheet = workbook.createSheet("CEO Summary");

    	 summarySheet.createRow(0).createCell(0)
    	         .setCellValue("PEOPLEFLOW - CEO PAYROLL SUMMARY");

    	 summarySheet.createRow(1).createCell(0)
    	         .setCellValue("Payroll Month");

    	 summarySheet.getRow(1).createCell(1)
    	         .setCellValue(monthLabel);

    	 double totalGross = 0;
    	 double totalDeductions = 0;
    	 double totalNet = 0;
    	 double totalEmployerContribution = 0;
    	 double totalCtc = 0;
    	 double totalEmployeePf = 0;
    	 double totalEmployeeEsi = 0;
    	 double totalPt = 0;
    	 double totalEmployerPf = 0;
    	 double totalEmployerEps = 0;
    	 double totalEmployerEsi = 0;
    	 double totalPayableDays = 0;
    	 double totalWorkingDays = 0;

    	 for (Payroll payroll : payrolls) {

    	     totalGross += payroll.getGrossEarning();
    	     totalDeductions += payroll.getTotalDeductions();
    	     totalNet += payroll.getNetSalary();
    	     totalEmployerContribution += payroll.getTotalEmployerContribution();
    	     totalCtc += payroll.getCtc();

    	     totalEmployeePf += payroll.getPf();
    	     totalEmployeeEsi += payroll.getEmployeeEsi();
    	     totalPt += payroll.getProfessionalTax();

    	     totalEmployerPf += payroll.getEmployerPf();
    	     totalEmployerEps += payroll.getEmployerEps();
    	     totalEmployerEsi += payroll.getEmployerEsi();

    	     totalPayableDays += payroll.getPayableDays();
    	     totalWorkingDays += payroll.getWorkingDays();
    	 }

    	 String[][] summary = {
    	     {"Total Employees", String.valueOf(payrolls.size())},
    	     {"Total Gross Earnings", String.valueOf(totalGross)},
    	     {"Total Employee Deductions", String.valueOf(totalDeductions)},
    	     {"Total Net Salary", String.valueOf(totalNet)},
    	     {"Total Employer Contribution", String.valueOf(totalEmployerContribution)},
    	     {"Total CTC", String.valueOf(totalCtc)},
    	     {"Total Employee PF", String.valueOf(totalEmployeePf)},
    	     {"Total Employee ESI", String.valueOf(totalEmployeeEsi)},
    	     {"Total Professional Tax", String.valueOf(totalPt)},
    	     {"Total Employer PF", String.valueOf(totalEmployerPf)},
    	     {"Total Employer EPS", String.valueOf(totalEmployerEps)},
    	     {"Total Employer ESI", String.valueOf(totalEmployerEsi)},
    	     {"Total Payable Days", String.valueOf(totalPayableDays)},
    	     {"Total Working Days", String.valueOf(totalWorkingDays)}
    	 };

    	 for (int i = 0; i < summary.length; i++) {

    	     Row row = summarySheet.createRow(i + 3);

    	     row.createCell(0).setCellValue(summary[i][0]);
    	     row.createCell(1).setCellValue(summary[i][1]);
    	 }

    	 summarySheet.autoSizeColumn(0);
    	 summarySheet.autoSizeColumn(1);
    	 
    	// =========================
    	// CEO SUMMARY FORMATTING
    	// =========================

    	org.apache.poi.ss.usermodel.CellStyle summaryLabelStyle =
    	        workbook.createCellStyle();

    	org.apache.poi.ss.usermodel.Font summaryLabelFont =
    	        workbook.createFont();

    	summaryLabelFont.setBold(true);
    	summaryLabelFont.setFontHeightInPoints((short) 11);

    	summaryLabelStyle.setFont(summaryLabelFont);

    	org.apache.poi.ss.usermodel.CellStyle currencyStyle =
    	        workbook.createCellStyle();

    	currencyStyle.setDataFormat(
    	        workbook.createDataFormat().getFormat("\"₹\" #,##0.00"));

    	org.apache.poi.ss.usermodel.CellStyle currencyBoldStyle =
    	        workbook.createCellStyle();

    	currencyBoldStyle.setDataFormat(
    	        workbook.createDataFormat().getFormat("\"₹\" #,##0.00"));

    	org.apache.poi.ss.usermodel.Font currencyBoldFont =
    	        workbook.createFont();

    	currencyBoldFont.setBold(true);

    	currencyBoldStyle.setFont(currencyBoldFont);

    	// Apply formatting
    	for (int i = 0; i < summary.length; i++) {

    	    Row row = summarySheet.getRow(i + 3);

    	    row.getCell(0).setCellStyle(summaryLabelStyle);

    	    String label = summary[i][0];

    	    if (!label.equals("Total Employees")
    	            && !label.equals("Total Payable Days")
    	            && !label.equals("Total Working Days")) {

    	        double amount =
    	                Double.parseDouble(summary[i][1]);

    	        row.getCell(1).setCellValue(amount);

    	        if (label.equals("Total Net Salary")
    	                || label.equals("Total CTC")
    	                || label.equals("Total Gross Earnings")) {

    	            row.getCell(1).setCellStyle(currencyBoldStyle);
    	        } else {
    	            row.getCell(1).setCellStyle(currencyStyle);
    	        }
    	    }
    	}

    	 summarySheet.createFreezePane(0, 3);

    	    return workbook;
    	}
}
