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
			try {
				ClassPathResource logoResource = new ClassPathResource("static/logo.png");
				if (logoResource.exists()) {
					Image logo = Image.getInstance(logoResource.getURL());
					logo.scaleToFit(100, 100);
					logo.setAlignment(Element.ALIGN_CENTER);
					document.add(logo);
				}
			} catch (Exception e) {
				logger.warn("Logo not found or failed to load: {}", e.getMessage());
			}

			document.add(new Paragraph(" "));

			// Fonts
			Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
			Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
			Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

			// Date & Month
			LocalDate today = LocalDate.now();
			String date = today.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
			String monthYear = today.format(DateTimeFormatter.ofPattern("MMMM yyyy"));

			// Company Name
			Paragraph company = new Paragraph("Renwion", headerFont);
			company.setAlignment(Element.ALIGN_CENTER);
			document.add(company);

			// Title
			Paragraph title = new Paragraph("EMPLOYEE PAYSLIP", titleFont);
			title.setAlignment(Element.ALIGN_CENTER);

			document.add(new Paragraph(" "));
			document.add(title);

			// employee details
			document.add(new Paragraph("Employee ID: " + payslip.getId(), headerFont));
			document.add(new Paragraph("Employee Name: " + payslip.getName(), headerFont));
			document.add(new Paragraph("Month: " + monthYear, normalFont));
			document.add(new Paragraph("Date: " + date, normalFont));

			document.add(new Paragraph(" "));

			// Table
			PdfPTable table = new PdfPTable(2);
			table.setWidthPercentage(100);

			addRow(table, "Basic Salary", "₹ " + String.format("%.2f", payslip.getBasicSalary()));
			addRow(table, "HRA (20%)", "₹ " + String.format("%.2f", payslip.getHra()));
			addRow(table, "PF (12%)", "₹ " + String.format("%.2f", payslip.getPf()));
			addRow(table, "Leave Deduction", "₹ " + String.format("%.2f", payslip.getLeaveDeduction()));
			addRow(table, "Net Salary", "₹ " + String.format("%.2f", payslip.getNetSalary()));

			document.add(table);

			document.add(new Paragraph(" "));
			document.add(new Paragraph("Authorized Signature", normalFont));

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
