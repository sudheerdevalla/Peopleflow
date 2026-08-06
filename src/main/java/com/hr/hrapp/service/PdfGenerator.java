package com.hr.hrapp.service;

import com.hr.hrapp.entity.Salary;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpServletResponse;

public class PdfGenerator {

    private static final Logger logger =
            LoggerFactory.getLogger(PdfGenerator.class);

    public static void generate(
            HttpServletResponse response,
            Salary salary) throws Exception {

        if (salary == null) {

            logger.warn(
                    "No salary provided to PdfGenerator.generate");

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Salary not found");

            return;
        }

        Document document =
                new Document(
                        PageSize.A4,
                        36,
                        36,
                        36,
                        36);

        PdfWriter.getInstance(
                document,
                response.getOutputStream());

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

            logger.warn(
                    "Logo not found: {}",
                    e.getMessage());
        }

        Font titleFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        18);

        Font normalFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        11);

        Font boldFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        12);

        // Company Header
        Paragraph company =
                new Paragraph(
                        "RENWION CLEAN ENVIRO SOLUTIONS PRIVATE LIMITED",
                        titleFont);

        company.setAlignment(
                Element.ALIGN_CENTER);

        document.add(company);

        Paragraph subtitle =
                new Paragraph(
                        "EMPLOYEE PAYSLIP",
                        boldFont);

        subtitle.setAlignment(
                Element.ALIGN_CENTER);

        document.add(subtitle);

        document.add(
                new Paragraph(" "));

        // Employee Details
        PdfPTable infoTable =
                new PdfPTable(2);

        infoTable.setWidthPercentage(100);

        addRow(
                infoTable,
                "Payslip No",
                "PAY-" + salary.getId());

        addRow(
                infoTable,
                "Employee ID",
                String.valueOf(
                        salary.getEmployeeId()));

        addRow(
                infoTable,
                "Pay Period",
                salary.getMonth());

        document.add(infoTable);

        document.add(
                new Paragraph(" "));

        // Salary Details
        Paragraph salaryHeading =
                new Paragraph(
                        "SALARY DETAILS",
                        boldFont);

        document.add(
                salaryHeading);

        document.add(
                new Paragraph(" "));

        PdfPTable table =
                new PdfPTable(2);

        table.setWidthPercentage(100);

        addRow(
                table,
                "Basic Salary",
                String.format(
                        "₹ %.2f",
                        salary.getBasicSalary()));

        addRow(
                table,
                "Hike Amount",
                String.format(
                        "₹ %.2f",
                        salary.getHikeAmount()));

        PdfPCell labelCell =
                new PdfPCell(
                        new Phrase(
                                "Net Salary"));

        labelCell.setBackgroundColor(
                BaseColor.LIGHT_GRAY);

        PdfPCell valueCell =
                new PdfPCell(
                        new Phrase(
                                String.format(
                                        "₹ %.2f",
                                        salary.getNetSalary())));

        valueCell.setBackgroundColor(
                BaseColor.GREEN);

        table.addCell(
                labelCell);

        table.addCell(
                valueCell);

        document.add(table);

        document.add(
                new Paragraph(" "));
        document.add(
                new Paragraph(" "));
        document.add(
                new Paragraph(" "));

        Paragraph sign =
                new Paragraph(
                        "Authorized Signature",
                        boldFont);

        sign.setAlignment(
                Element.ALIGN_RIGHT);

        document.add(sign);

        document.add(
                new Paragraph(" "));

        Paragraph footer =
                new Paragraph(
                        "This is a computer generated payslip and does not require physical signature.",
                        normalFont);

        footer.setAlignment(
                Element.ALIGN_CENTER);

        document.add(
                footer);

        document.close();
    }

    private static void addRow(
            PdfPTable table,
            String key,
            String value) {

        table.addCell(
                new PdfPCell(
                        new Phrase(key)));

        table.addCell(
                new PdfPCell(
                        new Phrase(value)));
    }
}