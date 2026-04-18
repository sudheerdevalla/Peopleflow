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

@Service

public class EmployeePayslipPdfService {

	public byte[] generatePayslipPdf(EmployeePayslip payslip, Long id) {

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		try {
			Document document = new Document();
			PdfWriter.getInstance(document, out);

			document.open();
     
			//Image logo = Image.getInstance(getClass().getResource("src/main/resources/static/logo.png"));
			
			Image logo = Image.getInstance("src/main/resources/static/logo.png");
        

			logo.scaleToFit(100, 100); // size
			logo.setAlignment(Element.ALIGN_CENTER);

			document.add(logo);
			document.add(new Paragraph(" "));
			
			

			// Fonts
			Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
			Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
			Font normalFont = new Font(Font.FontFamily.HELVETICA, 12);

			// 📅 Date & Month
			LocalDate today = LocalDate.now();
			String date = today.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
			String monthYear = today.format(DateTimeFormatter.ofPattern("MMMM yyyy"));

			// 🏢 Company Name
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

			addRow(table, "Basic Salary", "₹ " + payslip.getBasicSalary());
			addRow(table, "HRA (20%)", "₹ " + payslip.getHra());
			addRow(table, "PF (12%)", "₹ " + payslip.getPf());
			addRow(table, "Leave Deduction", "₹ " + payslip.getLeaveDeduction());
			addRow(table, "Net Salary", "₹ " + payslip.getNetSalary());

			document.add(table);

			document.add(new Paragraph(" "));
			document.add(new Paragraph("Authorized Signature", normalFont));

			document.close();

		} catch (Exception e) {
			e.printStackTrace();
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
