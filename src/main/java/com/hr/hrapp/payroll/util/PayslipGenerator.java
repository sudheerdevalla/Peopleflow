package com.hr.hrapp.payroll.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.payroll.entity.Payroll;
import com.hr.hrapp.repository.EmployeeRepository;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class PayslipGenerator {

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

        	    System.out.println(
        	            "DOB is NULL for employee: "
        	            + employee.getName());

        	    password =
        	            firstTwoLetters + "00";
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

            // =========================
            // COMPANY TITLE
            // =========================

            Font titleFont =
                    FontFactory.getFont(
                            FontFactory.HELVETICA_BOLD,
                            22,
                            BaseColor.BLUE);

            Paragraph title =
                    new Paragraph(
                            "Renwion",
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

            Paragraph subTitle =
                    new Paragraph(
                            "Employee Salary Payslip",
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
                    BaseColor.YELLOW);

            salaryTable.addCell(netSalaryCell);

            document.add(salaryTable);

            // =========================
            // FOOTER
            // =========================

            document.add(new Paragraph(" "));

            Paragraph footer =
                    new Paragraph(
                            "This is a system generated payslip from Renwion HRMS.");

            footer.setAlignment(
                    Element.ALIGN_CENTER);

            document.add(footer);

            document.close();

        } catch (Exception e) {

            e.printStackTrace();
        }

        return new ByteArrayInputStream(
                out.toByteArray());
    }
}