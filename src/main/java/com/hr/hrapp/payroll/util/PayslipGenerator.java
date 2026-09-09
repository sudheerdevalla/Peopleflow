package com.hr.hrapp.payroll.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.payroll.entity.Payroll;
//import com.hr.hrapp.repository.EmployeeRepository;
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


public class PayslipGenerator {

    private static final Logger logger = LoggerFactory.getLogger(PayslipGenerator.class);

    public static ByteArrayInputStream generatePayslip(
            Payroll payroll,
            Employee employee,
            long sickLeaveCount,
            long annualLeaveCount) {

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
                java.io.InputStream logoStream =
                        PayslipGenerator.class
                                .getResourceAsStream("/static/images/logo.png");

                if (logoStream != null) {
                    Image logo = Image.getInstance(
                            logoStream.readAllBytes());

                    logo.scaleToFit(90, 90);
                    logo.setAlignment(Element.ALIGN_CENTER);

                    document.add(logo);
                } else {
                    logger.warn("Renwion logo not found at /static/images/logo.png");
                }

            } catch (Exception e) {
                logger.warn("Failed to add Renwion logo to payslip", e);
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

         Font sectionFont = FontFactory.getFont(
                 FontFactory.HELVETICA_BOLD,
                 11,
                 BaseColor.WHITE);

         PdfPTable employeeTable = new PdfPTable(4);
         employeeTable.setWidthPercentage(100);
         employeeTable.setSpacingBefore(12f);
         employeeTable.setSpacingAfter(12f);
         employeeTable.setWidths(new float[]{1.2f, 2.0f, 1.2f, 2.0f});

         // Section heading
         PdfPCell employeeHeader = new PdfPCell(
                 new Phrase("EMPLOYEE INFORMATION", sectionFont));

         employeeHeader.setColspan(4);
         employeeHeader.setBackgroundColor(new BaseColor(0, 102, 153));
         employeeHeader.setPadding(7f);
         employeeHeader.setHorizontalAlignment(Element.ALIGN_LEFT);

         employeeTable.addCell(employeeHeader);

         // Employee Name
         employeeTable.addCell("Employee Name");
         employeeTable.addCell(
                 employee.getName() == null ? "-" : employee.getName());

         // Employee Code
         employeeTable.addCell("Employee Code");
         employeeTable.addCell(
                 employee.getEmployeeCode() == null
                         ? "-"
                         : employee.getEmployeeCode());

         // Employee ID
         employeeTable.addCell("Employee ID");
         employeeTable.addCell(
                 employee.getEmpId() == null
                         ? "-"
                         : String.valueOf(employee.getEmpId()));

         // Department
         employeeTable.addCell("Department");
         employeeTable.addCell(
                 employee.getDepartment() == null
                         ? "-"
                         : employee.getDepartment());

         // Designation
         employeeTable.addCell("Designation");
         employeeTable.addCell(
                 employee.getDesignation() == null
                         ? "-"
                         : employee.getDesignation());

         // Joining Date
         employeeTable.addCell("Joining Date");
         employeeTable.addCell(
                 employee.getJoiningDate() == null
                         ? "-"
                         : employee.getJoiningDate().toString());

         // PAN
         employeeTable.addCell("PAN");
         employeeTable.addCell(
                 employee.getPanNumber() == null
                         ? "-"
                         : employee.getPanNumber());

         // UAN
         employeeTable.addCell("UAN");
         employeeTable.addCell(
                 employee.getUanNumber() == null
                         ? "-"
                         : employee.getUanNumber());

         // Bank
         employeeTable.addCell("Bank Name");
         employeeTable.addCell(
                 employee.getBankName() == null
                         ? "-"
                         : employee.getBankName());

         // IFSC
         employeeTable.addCell("IFSC");
         employeeTable.addCell(
                 employee.getIfsc() == null
                         ? "-"
                         : employee.getIfsc());

         // Account Number
         employeeTable.addCell("Account Number");

         String accountNumber = employee.getAccountNumber();

         String maskedAccount = "-";

         if (accountNumber != null && !accountNumber.isBlank()) {
             if (accountNumber.length() > 4) {
                 maskedAccount =
                         "XXXX" +
                         accountNumber.substring(accountNumber.length() - 4);
             } else {
                 maskedAccount = accountNumber;
             }
         }

         employeeTable.addCell(maskedAccount);

         // Pay Period
         employeeTable.addCell("Pay Period");
         employeeTable.addCell(
                 payroll.getMonth() == null
                         ? "-"
                         : payroll.getMonth());

         // Payment Status
         employeeTable.addCell("Payroll Status");
         employeeTable.addCell(
                 payroll.getStatus() == null
                         ? "FINALIZED"
                         : payroll.getStatus());

         document.add(employeeTable);

      // =========================
      // SALARY DETAILS
      // =========================

      Font tableHeaderFont = FontFactory.getFont(
              FontFactory.HELVETICA_BOLD,
              10,
              BaseColor.WHITE);

      Font boldFont = FontFactory.getFont(
              FontFactory.HELVETICA_BOLD,
              10,
              BaseColor.BLACK);

      Font normalFont = FontFactory.getFont(
              FontFactory.HELVETICA,
              10,
              BaseColor.BLACK);

      PdfPTable salaryTable = new PdfPTable(3);
      salaryTable.setWidthPercentage(100);
      salaryTable.setSpacingBefore(8f);
      salaryTable.setSpacingAfter(12f);
      salaryTable.setWidths(new float[]{2.8f, 1.2f, 1.5f});

      // =========================
      // TABLE HEADER
      // =========================

      PdfPCell earningsHeader = new PdfPCell(
              new Phrase("EARNINGS", tableHeaderFont));

      earningsHeader.setColspan(2);
      earningsHeader.setBackgroundColor(
              new BaseColor(0, 102, 153));
      earningsHeader.setPadding(6f);

      PdfPCell amountHeader = new PdfPCell(
              new Phrase("AMOUNT", tableHeaderFont));

      amountHeader.setBackgroundColor(
              new BaseColor(0, 102, 153));
      amountHeader.setPadding(6f);

      salaryTable.addCell(earningsHeader);
      salaryTable.addCell(amountHeader);

      // =========================
      // EARNINGS
      // =========================

      salaryTable.addCell("Basic Salary");
      salaryTable.addCell("");
      salaryTable.addCell(
              "₹ " + payroll.getBasicSalary());

      salaryTable.addCell("HRA");
      salaryTable.addCell("");
      salaryTable.addCell(
              "₹ " + payroll.getHra());

      salaryTable.addCell("Bonus");
      salaryTable.addCell("");
      salaryTable.addCell(
              "₹ " + payroll.getBonus());

      salaryTable.addCell("Approved Travel");
      salaryTable.addCell("");
      salaryTable.addCell(
              "₹ " + payroll.getApprovedAdditions());

      double travelAllowance =
              payroll.getTravelAllowance() == null
                      ? 0.0
                      : payroll.getTravelAllowance();

      salaryTable.addCell("Travel Allowance");
      salaryTable.addCell("");
      salaryTable.addCell(
              "₹ " + travelAllowance);

      // =========================
      // TOTAL EARNINGS
      // =========================

      PdfPCell totalEarningsLabel =
              new PdfPCell(new Phrase(
                      "TOTAL EARNINGS", boldFont));

      totalEarningsLabel.setColspan(2);
      totalEarningsLabel.setPadding(6f);

      salaryTable.addCell(totalEarningsLabel);

      PdfPCell totalEarningsValue =
              new PdfPCell(new Phrase(
                      "₹ " + payroll.getGrossSalary(),
                      boldFont));

      totalEarningsValue.setPadding(6f);

      salaryTable.addCell(totalEarningsValue);

      // =========================
      // DEDUCTIONS HEADER
      // =========================

      PdfPCell deductionsHeader =
              new PdfPCell(new Phrase(
                      "DEDUCTIONS", tableHeaderFont));

      deductionsHeader.setColspan(2);
      deductionsHeader.setBackgroundColor(
              new BaseColor(90, 90, 90));
      deductionsHeader.setPadding(6f);

      salaryTable.addCell(deductionsHeader);

      PdfPCell deductionAmountHeader =
              new PdfPCell(new Phrase(
                      "AMOUNT", tableHeaderFont));

      deductionAmountHeader.setBackgroundColor(
              new BaseColor(90, 90, 90));
      deductionAmountHeader.setPadding(6f);

      salaryTable.addCell(deductionAmountHeader);

      // =========================
      // DEDUCTIONS
      // =========================

      salaryTable.addCell("PF Deduction");
      salaryTable.addCell("");
      salaryTable.addCell(
              "₹ " + payroll.getPf());

      salaryTable.addCell("Tax Deduction");
      salaryTable.addCell("");
      salaryTable.addCell(
              "₹ " + payroll.getTax());

      salaryTable.addCell("Total Deductions");
      salaryTable.addCell("");
      salaryTable.addCell(
              "₹ " + payroll.getDeductions());

      // =========================
      // PAYABLE DAYS
      // =========================

      salaryTable.addCell("Payable Days");
      salaryTable.addCell("");
      salaryTable.addCell(
              payroll.getPayableDays()
              + " / "
              + payroll.getWorkingDays());

      // =========================
      // NET SALARY
      // =========================

      PdfPCell netLabel =
              new PdfPCell(new Phrase(
                      "NET SALARY PAYABLE", boldFont));

      netLabel.setColspan(2);
      netLabel.setPadding(8f);
      netLabel.setBackgroundColor(
              new BaseColor(220, 245, 220));

      salaryTable.addCell(netLabel);

      PdfPCell netValue =
              new PdfPCell(new Phrase(
                      "₹ " + payroll.getNetSalary(),
                      boldFont));

      netValue.setPadding(8f);
      netValue.setBackgroundColor(
              new BaseColor(220, 245, 220));

      salaryTable.addCell(netValue);

      document.add(salaryTable);
   // =========================
   // PAYROLL SUMMARY
   // =========================

   Font summaryFont = FontFactory.getFont(
           FontFactory.HELVETICA,
           10,
           BaseColor.BLACK);

   Font summaryBoldFont = FontFactory.getFont(
           FontFactory.HELVETICA_BOLD,
           10,
           BaseColor.BLACK);

   PdfPTable summaryTable = new PdfPTable(2);
   summaryTable.setWidthPercentage(100);
   summaryTable.setSpacingBefore(5f);
   summaryTable.setSpacingAfter(10f);
   summaryTable.setWidths(new float[]{3f, 7f});

   PdfPCell summaryTitle = new PdfPCell(
           new Phrase("PAYROLL SUMMARY", summaryBoldFont));
   summaryTitle.setColspan(2);
   summaryTitle.setPadding(6f);
   summaryTable.addCell(summaryTitle);

   summaryTable.addCell(
           new PdfPCell(new Phrase("Payroll Status", summaryBoldFont)));
   summaryTable.addCell(
           new PdfPCell(new Phrase(
                   payroll.getStatus() == null
                           ? ""
                           : payroll.getStatus().toString(),
                   summaryFont)));

   summaryTable.addCell(
           new PdfPCell(new Phrase("Payable Days", summaryBoldFont)));
   summaryTable.addCell(
           new PdfPCell(new Phrase(
                   String.valueOf(payroll.getPayableDays()),
                   summaryFont)));

   summaryTable.addCell(
           new PdfPCell(new Phrase("Working Days", summaryBoldFont)));
   summaryTable.addCell(
           new PdfPCell(new Phrase(
                   String.valueOf(payroll.getWorkingDays()),
                   summaryFont)));

   document.add(summaryTable);
// =========================
// LEAVE SUMMARY
// =========================

Font leaveHeaderFont = FontFactory.getFont(
        FontFactory.HELVETICA_BOLD,
        10,
        BaseColor.WHITE);

Font leaveBoldFont = FontFactory.getFont(
        FontFactory.HELVETICA_BOLD,
        10,
        BaseColor.BLACK);

PdfPTable leaveTable = new PdfPTable(3);
leaveTable.setWidthPercentage(100);
leaveTable.setSpacingBefore(5f);
leaveTable.setSpacingAfter(10f);
leaveTable.setWidths(new float[]{4f, 2f, 4f});

// HEADER
PdfPCell leaveHeader = new PdfPCell(
        new Phrase("LEAVE SUMMARY", leaveHeaderFont));
leaveHeader.setColspan(3);
leaveHeader.setBackgroundColor(new BaseColor(0, 102, 153));
leaveHeader.setPadding(6f);
leaveTable.addCell(leaveHeader);

leaveTable.addCell(
        new PdfPCell(new Phrase("Leave Type", leaveBoldFont)));

leaveTable.addCell(
        new PdfPCell(new Phrase("Days", leaveBoldFont)));

leaveTable.addCell(
        new PdfPCell(new Phrase("Status", leaveBoldFont)));

// SICK LEAVE
leaveTable.addCell("Sick Leave");
leaveTable.addCell(String.valueOf(sickLeaveCount));
leaveTable.addCell("APPROVED");

// ANNUAL LEAVE
leaveTable.addCell("Annual Leave");
leaveTable.addCell(String.valueOf(annualLeaveCount));
leaveTable.addCell("APPROVED");

document.add(leaveTable);


//=========================
//AMOUNT IN WORDS
//=========================

Font amountWordsFont = FontFactory.getFont(
     FontFactory.HELVETICA_BOLD,
     10,
     BaseColor.BLACK);

long netAmount = Math.round(payroll.getNetSalary());

PdfPTable amountWordsTable = new PdfPTable(1);
amountWordsTable.setWidthPercentage(100);
amountWordsTable.setSpacingBefore(5f);
amountWordsTable.setSpacingAfter(12f);

PdfPCell amountWordsCell = new PdfPCell(
     new Phrase(
             "Amount in Words: "
             + NumberToWordsConverter.convert(netAmount)
             + " Only",
             amountWordsFont));

amountWordsCell.setPadding(8f);
amountWordsTable.addCell(amountWordsCell);

document.add(amountWordsTable);
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
    

    // =========================
    // NUMBER TO WORDS CONVERTER
    // =========================

    private static class NumberToWordsConverter {

        private static final String[] UNITS = {
                "", "One", "Two", "Three", "Four", "Five",
                "Six", "Seven", "Eight", "Nine", "Ten",
                "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen",
                "Sixteen", "Seventeen", "Eighteen", "Nineteen"
        };

        private static final String[] TENS = {
                "", "", "Twenty", "Thirty", "Forty",
                "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
        };

        public static String convert(long number) {

            if (number == 0) {
                return "Zero Rupees";
            }

            if (number < 0) {
                return "Minus " + convert(-number);
            }

            StringBuilder result = new StringBuilder();

            if (number >= 10000000) {
                result.append(convert(number / 10000000))
                      .append(" Crore ");
                number %= 10000000;
            }

            if (number >= 100000) {
                result.append(convert(number / 100000))
                      .append(" Lakh ");
                number %= 100000;
            }

            if (number >= 1000) {
                result.append(convert(number / 1000))
                      .append(" Thousand ");
                number %= 1000;
            }

            if (number >= 100) {
                result.append(convert(number / 100))
                      .append(" Hundred ");
                number %= 100;
            }

            if (number >= 20) {
                result.append(TENS[(int) (number / 10)])
                      .append(" ");
                number %= 10;
            }

            if (number > 0) {
                result.append(UNITS[(int) number])
                      .append(" ");
            }

            return result.toString().trim() + " Rupees";
        }
    }

}
