package com.hr.hrapp.payroll.report;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.hr.hrapp.payroll.entity.Payroll;
import com.hr.hrapp.payroll.repository.PayrollRepository;
import com.hr.hrapp.util.PayrollMonthUtil;

import jakarta.mail.internet.MimeMessage;

@Service
public class CEOReportService {

    @Autowired
    private PayrollRepository payrollRepository;

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
          XSSFSheet sheet = workbook.createSheet("Payroll Report");

          Row header = sheet.createRow(0);
          header.createCell(0).setCellValue("Month");
          header.createCell(1).setCellValue("Employee ID");
          header.createCell(2).setCellValue("Employee");
          header.createCell(3).setCellValue("Payable Days");
          header.createCell(4).setCellValue("Working Days");
          header.createCell(5).setCellValue("Basic Salary");
          header.createCell(6).setCellValue("HRA");
          header.createCell(7).setCellValue("Bonus");
          header.createCell(8).setCellValue("Approved Additions");
          header.createCell(9).setCellValue("Travel Allowance");
          header.createCell(10).setCellValue("Gross Salary");
          header.createCell(11).setCellValue("PF");
          header.createCell(12).setCellValue("Tax");
          header.createCell(13).setCellValue("Deductions");
          header.createCell(14).setCellValue("Net Salary");
          header.createCell(15).setCellValue("Status");

          int rowNum = 1;
          for (Payroll payroll : payrolls) {
              Row row = sheet.createRow(rowNum++);
              row.createCell(0).setCellValue(monthLabel);
              row.createCell(1).setCellValue(payroll.getEmployeeId());
              row.createCell(2).setCellValue(payroll.getEmployeeName());
              row.createCell(3).setCellValue(payroll.getPayableDays());
              row.createCell(4).setCellValue(payroll.getWorkingDays());
              row.createCell(5).setCellValue(payroll.getBasicSalary());
              row.createCell(6).setCellValue(payroll.getHra());
              row.createCell(7).setCellValue(payroll.getBonus());
              row.createCell(8).setCellValue(payroll.getApprovedAdditions());
              row.createCell(9).setCellValue(payroll.getTravelAllowance() == null ? 0.0 : payroll.getTravelAllowance());
              row.createCell(10).setCellValue(payroll.getGrossSalary());
              row.createCell(11).setCellValue(payroll.getPf());
              row.createCell(12).setCellValue(payroll.getTax());
              row.createCell(13).setCellValue(payroll.getDeductions());
              row.createCell(14).setCellValue(payroll.getNetSalary());
              row.createCell(15).setCellValue(payroll.getStatus());
          }

          for (int i = 0; i <= 15; i++) {
              sheet.autoSizeColumn(i);
          }
          return workbook;
      }
}
