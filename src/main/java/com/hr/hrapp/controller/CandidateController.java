package com.hr.hrapp.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.hr.hrapp.entity.Candidate;
import com.hr.hrapp.repository.CandidateRepository;
import com.hr.hrapp.service.EmailService;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.itextpdf.text.Document;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
@Controller
@RequestMapping("/candidates")
public class CandidateController {

    @Autowired
    private CandidateRepository candidateRepository;
    
    @Autowired
    private EmailService emailService;

    // Open Form
    @GetMapping("/add")
    public String addCandidateForm(Model model) {

        model.addAttribute(
                "candidate",
                new Candidate());

        return "add-candidate";
    }

    // Save Candidate
    @PostMapping("/save")
    public String saveCandidate(
            @ModelAttribute Candidate candidate,
            @RequestParam("resume") MultipartFile file)
            throws IOException {

        candidate.setStatus("APPLIED");

        // Resume Upload
        if (!file.isEmpty()) {

            String fileName =
                    System.currentTimeMillis()
                    + "_"
                    + file.getOriginalFilename();

            String uploadDir =
                    System.getProperty("user.dir")
                    + "/uploads/resumes/";

            File dir = new File(uploadDir);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            file.transferTo(
                    new File(uploadDir + fileName)
            );

            candidate.setResumeFile(fileName);
        }

        candidateRepository.save(candidate);

        return "redirect:/candidates/list";
    }

    // Candidate List
    @GetMapping("/list")
    public String candidateList(Model model) {

        List<Candidate> candidates =
                candidateRepository.findAll();

        model.addAttribute(
                "candidates",
                candidates);

        return "candidate-list";
    }
    @GetMapping("/status/{id}/{status}")
    public String updateStatus(
            @PathVariable Long id,
            @PathVariable String status) {

        Candidate candidate =
                candidateRepository.findById(id)
                .orElse(null);

        if (candidate == null) {
            return "redirect:/candidates/list";
        }

        String current = candidate.getStatus();

        boolean valid =
                (current.equals("APPLIED")
                        && status.equals("SHORTLISTED"))

                || (current.equals("SHORTLISTED")
                        && status.equals("INTERVIEW_SCHEDULED"))

                || (current.equals("INTERVIEW_SCHEDULED")
                        && (status.equals("SELECTED")
                        || status.equals("REJECTED")));

        if (!valid) {
            return "redirect:/candidates/list";
        }

        candidate.setStatus(status);

        candidateRepository.save(candidate);

        System.out.println(
                "Candidate Updated = "
                + status);

        candidate.setStatus(status);

        candidateRepository.save(candidate);

        System.out.println(
                "Candidate Updated = "
                + status);

        // SHORTLIST MAIL
        if(status.equals("SHORTLISTED")) {

            emailService.sendMail(
                    candidate.getEmail(),
                    "Application Shortlisted",
                    """
                    Dear %s,

                    Congratulations!

                    Your application for the position of %s has been shortlisted.

                    Regards,
                    HR Team
                    """
                    .formatted(
                            candidate.getName(),
                            candidate.getPositionApplied()));
        }

        // INTERVIEW MAIL
        if(status.equals("INTERVIEW_SCHEDULED")) {

            emailService.sendMail(
                    candidate.getEmail(),
                    "Interview Scheduled",
                    """
                    Dear %s,

                    Your interview has been scheduled for the position of %s.

                    Regards,
                    HR Team
                    """
                    .formatted(
                            candidate.getName(),
                            candidate.getPositionApplied()));
        }

        return "redirect:/candidates/list";
    }
    
    @GetMapping("/offer/{id}")
    public ResponseEntity<byte[]> generateOfferLetter(
            @PathVariable Long id) throws Exception {

        Candidate candidate =
                candidateRepository.findById(id)
                .orElseThrow();
        
        candidate.setStatus("OFFER_SENT");

        candidateRepository.save(candidate);

        Document document =
                new Document();

        ByteArrayOutputStream out =
                new ByteArrayOutputStream();

        PdfWriter.getInstance(
                document,
                out);

        document.open();

        document.add(
                new Paragraph(
                        "Renwion Clean Enviro Solutions Private Limited"));

        document.add(
                new Paragraph(
                        "--------------------------------------------------"));

        document.add(
                new Paragraph(
                        "OFFER LETTER"));

        document.add(
                new Paragraph(" "));

        document.add(
                new Paragraph(
                        "Date: "
                        + java.time.LocalDate.now()));

        document.add(
                new Paragraph(" "));

        document.add(
                new Paragraph(
                        "Dear "
                        + candidate.getName()
                        + ","));

        document.add(
                new Paragraph(" "));

        document.add(
                new Paragraph(
                        "We are delighted to inform you that you have been selected for the position of "
                        + candidate.getPositionApplied()
                        + " at Renwion Clean Enviro Solutions Private Limited."));

        document.add(
                new Paragraph(" "));

        document.add(
                new Paragraph(
                        "Your qualifications, skills, and interview performance have demonstrated that you will be a valuable addition to our organization."));

        document.add(
                new Paragraph(" "));
        
        String joiningDate = "";

        if(candidate.getJoiningDate() != null) {

            joiningDate =
                    candidate.getJoiningDate()
                    .format(
                        DateTimeFormatter.ofPattern("dd-MMM-yyyy"));

        } else {

            joiningDate = "To Be Announced";
        }

        document.add(
                new Paragraph(
                        "You are requested to join our organization on "
                        + joiningDate
                        + ". Additional onboarding instructions and required documents will be shared separately by our HR team."));

        document.add(
                new Paragraph(" "));

        document.add(
                new Paragraph(
                        "We look forward to having you as part of our growing team and wish you a successful career with us."));

        document.add(
                new Paragraph(" "));

        document.add(
                new Paragraph(
                        "Congratulations and welcome aboard!"));

        document.add(
                new Paragraph(" "));

        document.add(
                new Paragraph(
                        "Regards,"));

        document.add(
                new Paragraph(
                        "Human Resources"));

        document.add(
                new Paragraph(
                        "Renwion Clean Enviro Solutions Private Limited"));

        document.close();
        
        byte[] pdfData = out.toByteArray();

        String body =
                """
                Dear %s,

                Congratulations!

                We are excited to offer you the position of %s at PeopleFlow Technologies. We were impressed with your skills and experience, and we believe you will be a valuable addition to our team.

                Please find your Offer Letter attached.

                Welcome to Renwion Clean Enviro solutions Private Limited.

                Regards,
                HR Team
                """
                .formatted(
                        candidate.getName(),
                        candidate.getPositionApplied()
                );

        emailService.sendMailWithAttachment(
                candidate.getEmail(),
                "Offer Letter - Renwion Clean Enviro solutions Private Limited",
                body,
                pdfData,
                "OfferLetter.pdf");

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=OfferLetter.pdf")
                .contentType(
                        MediaType.APPLICATION_PDF)
                .body(pdfData);
    }
}