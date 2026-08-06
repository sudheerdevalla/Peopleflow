package com.hr.hrapp.payroll.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.payroll.entity.Payroll;
import com.hr.hrapp.repository.EmployeeRepository;
import com.itextpdf.text.BaseColor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Image;
import com.itextpdf.text.BaseColor;

public class PayslipGenerator {

    private static final Logger logger = LoggerFactory.getLogger(PayslipGenerator.class);

    public static ByteArrayInputStream generatePayslip(
            Payroll payroll, Employee employee) {

        Document document = new Document();

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        try {

        	// =========================
        	// PASSWORD LOGIC
        	// =========================

        	String employeeName =
        	        employee.getName();

        	String firstTwoLetters =
        	        employeeName.length() >= 2
        	                ? employeeName.substring(0, 2)
        	                : employeeName;

        	String password;

            	if (employee.getDateOfBirth() != null) {

        	    String year =
        	            String.valueOf(
        	                    employee.getDateOfBirth()
        	                            .getYear());

        	    String lastTwoDigits =
        	            year.substring(2);

        	    password =
        	            firstTwoLetters
        	            + lastTwoDigits;

            	} else {
            	    logger.warn("DOB is NULL for employee: {}", employee.getName());
            	    password = firstTwoLetters + "00";
            	}

            // =========================
            // PDF WRITER
            // =========================

            PdfWriter writer =
                    PdfWriter.getInstance(
                            document,
                            out);

            writer.setEncryption(
                    password.getBytes(),
                    password.getBytes(),
                    PdfWriter.ALLOW_PRINTING,
                    PdfWriter.STANDARD_ENCRYPTION_128);

            document.open();
            
            try {

                Image logo =
                        Image.getInstance(
                        "src/main/resources/static/images/logo.png");

                logo.scaleToFit(80, 80);

                logo.setAlignment(
                        Element.ALIGN_CENTER);

                document.add(logo);

            } catch (Exception e) {
                logger.warn("Failed to add logo to payslip: {}", e.getMessage());
            }

            // =========================
            // COMPANY TITLE
            // =========================

            Font titleFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            16,
                            BaseColor.BLUE);

            Paragraph title =
                    new Paragraph(
                            "RENWION CLEAN ENVIRO SOLUTIONS PRIVATE LIMITED",
                            titleFont);

            title.setAlignment(
                    Element.ALIGN_CENTER);

            document.add(title);

            // =========================
            // SUBTITLE
            // =========================

            Font subTitleFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA,
                            12,
                            BaseColor.DARK_GRAY);
            Paragraph address =
                    new Paragraph(
                    "Hyderabad, Telangana, India");

            address.setAlignment(
                    Element.ALIGN_CENTER);

            document.add(address);

            Paragraph subTitle =
                    new Paragraph(
                            "EMPLOYEE PAYSLIP",
                            subTitleFont);

            subTitle.setAlignment(
                    Element.ALIGN_CENTER);

            document.add(subTitle);

            document.add(new Paragraph(" "));

            // =========================
            // EMPLOYEE DETAILS
            // =========================

            PdfPTable employeeTable =
                    new PdfPTable(2);

            employeeTable.setWidthPercentage(100);

            employeeTable.setSpacingBefore(10f);

            employeeTable.setSpacingAfter(10f);

            employeeTable.addCell("Employee Name");

            employeeTable.addCell(
                    payroll.getEmployeeName());

            employeeTable.addCell("Month");

            employeeTable.addCell(
                    payroll.getMonth());

            employeeTable.addCell("Employee ID");

            employeeTable.addCell(
                    String.valueOf(
                            payroll.getEmployeeId()));

            document.add(employeeTable);

            // =========================
            // SALARY TABLE
            // =========================

            PdfPTable salaryTable =
                    new PdfPTable(2);

            salaryTable.setWidthPercentage(100);

            salaryTable.setSpacingBefore(10f);

            PdfPCell header =
                    new PdfPCell(
                            new Phrase(
                                    "Salary Breakdown"));

            header.setColspan(2);

            header.setBackgroundColor(
                    BaseColor.LIGHT_GRAY);

            header.setHorizontalAlignment(
                    Element.ALIGN_CENTER);

            salaryTable.addCell(header);

            salaryTable.addCell("Basic Salary");

            salaryTable.addCell(
                    "₹ " + payroll.getBasicSalary());

            salaryTable.addCell("HRA");

            salaryTable.addCell(
                    "₹ " + payroll.getHra());

            salaryTable.addCell("Bonus");

            salaryTable.addCell(
                    "₹ " + payroll.getBonus());

            salaryTable.addCell("PF Deduction");

            salaryTable.addCell(
                    "₹ " + payroll.getPf());

            salaryTable.addCell("Tax Deduction");

            salaryTable.addCell(
                    "₹ " + payroll.getTax());

            salaryTable.addCell("Net Salary");

            PdfPCell netSalaryCell =
                    new PdfPCell(
                            new Phrase(
                                    "₹ "
                                    + payroll.getNetSalary()));

            netSalaryCell.setBackgroundColor(
                    new BaseColor(144, 238, 144));

            salaryTable.addCell(netSalaryCell);

            document.add(salaryTable);
            
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            Paragraph sign =
                    new Paragraph(
                    "Authorized Signature");

            sign.setAlignment(
                    Element.ALIGN_RIGHT);

            document.add(sign);

            // =========================
            // FOOTER
            // =========================

            document.add(new Paragraph(" "));

            Paragraph footer =
                    new Paragraph(
                            "\"This is a computer generated payslip and does not require physical signature.\"");

            footer.setAlignment(
                    Element.ALIGN_CENTER);

            document.add(footer);

            document.close();

        } catch (Exception e) {
            logger.error("Failed to generate payslip PDF: {}", e.getMessage(), e);
        }

        return new ByteArrayInputStream(
                out.toByteArray());
    }
}