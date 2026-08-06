package com.hr.hrapp.service;

import com.hr.hrapp.dto.EmployeePayslip;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import com.itextpdf.text.BaseColor;

import com.itextpdf.text.Rectangle;

@Service

public class EmployeePayslipPdfService {

	private static final Logger logger = LoggerFactory.getLogger(EmployeePayslipPdfService.class);

	public byte[] generatePayslipPdf(EmployeePayslip payslip, Long id) {

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			Document document = new Document(PageSize.A4, 36, 36, 36, 36);
			PdfWriter.getInstance(document, out);

			document.open();

			// Load logo from classpath if available
			PdfPTable headerTable = new PdfPTable(2);
			headerTable.setWidthPercentage(100);
			
			// Fonts
						Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
						Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
						Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

			try {

			    ClassPathResource logoResource =
			            new ClassPathResource("static/logo.png");

			    if (logoResource.exists()) {

			        Image logo =
			                Image.getInstance(
			                        logoResource.getURL());

			        logo.scaleToFit(80, 80);

			        PdfPCell logoCell =
			                new PdfPCell(logo);

			        logoCell.setBorder(Rectangle.NO_BORDER);

			        headerTable.addCell(logoCell);
			    }

			} catch (Exception e) {

			    logger.warn(
			            "Logo load failed");
			}

			PdfPCell companyCell =
			        new PdfPCell();

			companyCell.setBorder(
			        Rectangle.NO_BORDER);

			companyCell.addElement(
			        new Paragraph(
			                "RENWION CLEAN ENVIRO SOLUTIONS PRIVATE LIMITED",
			                headerFont));

			companyCell.addElement(
			        new Paragraph(
			                "Hyderabad, Telangana, India",
			                normalFont));

			headerTable.addCell(companyCell);

			document.add(headerTable);

			document.add(new Paragraph(" "));

			

			// Date & Month
			LocalDate today = LocalDate.now();
			String date = today.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
			
			String monthYear = today.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
			
			String payslipNumber =
			        "PAY-"
			        + payslip.getId()
			        + "-"
			        + today.getMonthValue()
			        + "-"
			        + today.getYear();

			

			// Title
			Paragraph title = new Paragraph("EMPLOYEE PAYSLIP", titleFont);
			title.setAlignment(Element.ALIGN_CENTER);

			document.add(new Paragraph(" "));
			document.add(title);

			// employee details
			document.add(
			        new Paragraph(
			                "Employee ID : "
			                + payslip.getId(),
			                headerFont));

			document.add(
			        new Paragraph(
			                "Employee Name : "
			                + payslip.getName(),
			                headerFont));

			document.add(
			        new Paragraph(
			                "Pay Period : "
			                + monthYear,
			                normalFont));

			document.add(
			        new Paragraph(
			                "Generated Date : "
			                + date,
			                normalFont));

			document.add(
			        new Paragraph(
			                "Payslip No : "
			                + payslipNumber,
			                normalFont));

			document.add(new Paragraph(" "));
			
			document.add(
			        new Paragraph(
			                "EARNINGS",
			                headerFont));

			document.add(new Paragraph(" "));

			// Table
			PdfPTable table = new PdfPTable(2);
			table.setWidthPercentage(100);

			addRow(table,
			       "Basic Salary",
			       "₹ " + String.format("%.2f",
			       payslip.getBasicSalary()));

			addRow(table,
			       "HRA (20%)",
			       "₹ " + String.format("%.2f",
			       payslip.getHra()));

			addRow(table,
			       "PF (12%)",
			       "₹ " + String.format("%.2f",
			       payslip.getPf()));

			addRow(table,
			       "Leave Deduction",
			       "₹ " + String.format("%.2f",
			       payslip.getLeaveDeduction()));

			// Net Salary Highlight
			PdfPCell labelCell =
			        new PdfPCell(
			                new Phrase("Net Salary"));

			labelCell.setBackgroundColor(
			        BaseColor.LIGHT_GRAY);

			PdfPCell valueCell =
			        new PdfPCell(
			                new Phrase(
			                        "₹ "
			                        + String.format(
			                                "%.2f",
			                                payslip.getNetSalary())));

			valueCell.setBackgroundColor(
			        BaseColor.GREEN);

			table.addCell(labelCell);
			table.addCell(valueCell);

			document.add(table);

			document.add(new Paragraph(" "));
			document.add(new Paragraph(" "));

			document.add(
			        new Paragraph(
			                "This is a system generated payslip.",
			                normalFont));

			document.add(new Paragraph(" "));
			document.add(new Paragraph(" "));
			document.add(new Paragraph(" "));

			Paragraph sign =
			        new Paragraph(
			                "Authorized Signature",
			                headerFont);

			sign.setAlignment(
			        Element.ALIGN_RIGHT);

			document.add(sign);

			document.add(new Paragraph(" "));

			Paragraph footer =
			        new Paragraph(
			                "This is a computer generated payslip and does not require physical signature.",
			                normalFont);

			footer.setAlignment(
			        Element.ALIGN_CENTER);

			document.add(footer);

			Paragraph companyFooter =
			        new Paragraph(
			                "Renwion Clean Enviro Solutions Private Limited",
			                normalFont);

			companyFooter.setAlignment(
			        Element.ALIGN_CENTER);

			document.add(companyFooter);

			document.close();

		} catch (Exception e) {
			logger.error("Failed to generate payslip PDF: {}", e.getMessage(), e);
		}

		return out.toByteArray();

	}

	private void addRow(PdfPTable table, String key, String value) {

		PdfPCell cell1 = new PdfPCell(new Phrase(key));
		PdfPCell cell2 = new PdfPCell(new Phrase(value));

		table.addCell(cell1);
		table.addCell(cell2);
	}

}
