package com.hr.hrapp.payroll.report;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.hr.hrapp.payroll.entity.Payroll;
import com.hr.hrapp.payroll.repository.PayrollRepository;

import jakarta.mail.internet.MimeMessage;

@Service
public class CEOReportService {

    @Autowired
    private PayrollRepository payrollRepository;

    @Autowired
    private JavaMailSender mailSender;

    public void sendCEOReport() {

        try {

        	LocalDate previousMonth =
        	        LocalDate.now().minusMonths(1);

        	String currentMonth =
        	        previousMonth.getMonth()
        	        + " "
        	        + previousMonth.getYear();

        	List<Payroll> payrolls =
        	        payrollRepository.findByMonth(
        	                currentMonth);

            // =========================
            // EXCEL CREATE
            // =========================

            XSSFWorkbook workbook =
                    new XSSFWorkbook();

            XSSFSheet sheet =
                    workbook.createSheet(
                            "Payroll Report");

            // =========================
            // HEADER
            // =========================

            Row header =
                    sheet.createRow(0);

            header.createCell(0)
                    .setCellValue("Employee");

            header.createCell(1)
                    .setCellValue("Basic Salary");

            header.createCell(2)
                    .setCellValue("PF");

            header.createCell(3)
                    .setCellValue("Tax");

            header.createCell(4)
                    .setCellValue("Net Salary");

            // =========================
            // DATA
            // =========================

            int rowNum = 1;

            for(Payroll payroll : payrolls) {

                Row row =
                        sheet.createRow(rowNum++);

                row.createCell(0)
                        .setCellValue(
                                payroll.getEmployeeName());

                row.createCell(1)
                        .setCellValue(
                                payroll.getBasicSalary());

                row.createCell(2)
                        .setCellValue(
                                payroll.getPf());

                row.createCell(3)
                        .setCellValue(
                                payroll.getTax());

                row.createCell(4)
                        .setCellValue(
                                payroll.getNetSalary());
            }

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

            helper.setTo(
                    "asha.renwion@gmail.com");

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
}
