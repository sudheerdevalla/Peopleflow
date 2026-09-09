package com.hr.hrapp.service;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.entity.EmployeeDocument;
import com.hr.hrapp.repository.EmployeeDocumentRepository;
import com.hr.hrapp.repository.EmployeeRepository;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmployeeDocumentImportService {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}", Pattern.CASE_INSENSITIVE);
    private static final Pattern PAN_PATTERN = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]");
    private static final Pattern AADHAAR_PATTERN = Pattern.compile("\\b[0-9]{4}\\s?[0-9]{4}\\s?[0-9]{4}\\b");
    private static final Pattern IFSC_PATTERN = Pattern.compile("[A-Z]{4}0[A-Z0-9]{6}");
    private static final Pattern ACCOUNT_PATTERN = Pattern.compile("\\b[0-9]{9,18}\\b");
    private static final Pattern UAN_PATTERN = Pattern.compile("\\b[0-9]{12}\\b");
    private static final Pattern EMPLOYEE_CODE_PATTERN = Pattern.compile("(?:employee\\s*code|emp\\s*code)\\s*[:-]?\\s*([A-Z0-9\\-/]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern PF_PATTERN = Pattern.compile("(?:pf\\s*(?:no|number)?)\\s*[:-]?\\s*([A-Z0-9\\-/]+)", Pattern.CASE_INSENSITIVE);

    @Autowired
    private EmployeeDocumentRepository employeeDocumentRepository;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private AuditTrailService auditTrailService;

    @Transactional
    public EmployeeDocument importDocument(Long employeeId,
                                           String documentType,
                                           MultipartFile file,
                                           String reviewerUsername) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Document file is required");
        }

        try {
            byte[] bytes = file.getBytes();
            String hash = sha256(bytes);
            Optional<EmployeeDocument> duplicate = employeeDocumentRepository.findByFileHash(hash);
            if (duplicate.isPresent()) {
                throw new IllegalStateException("This document has already been uploaded and indexed");
            }

            String originalFileName = file.getOriginalFilename() == null ? "document" : file.getOriginalFilename();
            String storedFileName = System.currentTimeMillis() + "_" + originalFileName.replaceAll("[^A-Za-z0-9._-]", "_");
            Path uploadPath = Paths.get("uploads", "documents", "hr-review");
            Files.createDirectories(uploadPath);
            Files.copy(new ByteArrayInputStream(bytes), uploadPath.resolve(storedFileName), StandardCopyOption.REPLACE_EXISTING);

            EmployeeDocument document = new EmployeeDocument();
            document.setEmployeeId(employeeId);
            document.setDocumentType(documentType);
            document.setFileName(storedFileName);
            document.setOriginalFileName(originalFileName);
            document.setContentType(file.getContentType());
            document.setStoragePath(uploadPath.resolve(storedFileName).toString());
            document.setFileHash(hash);
            document.setUploadedAt(LocalDateTime.now());
            document.setReviewStatus("PENDING_REVIEW");

            String extractedText = extractText(originalFileName, bytes);
            if (extractedText == null || extractedText.isBlank()) {
                document.setExtractionStatus("MANUAL_REVIEW_REQUIRED");
                document.setValidationStatus("MANUAL_REVIEW_REQUIRED");
                document.setConflictStatus("UNKNOWN");
                document.setReviewNotes("No text could be extracted automatically. Manual HR review required.");
            } else {
                mapExtractedFields(document, extractedText);
                evaluateValidationAndConflicts(document, employee);
            }

            EmployeeDocument saved = employeeDocumentRepository.save(document);
            auditTrailService.record(
                    reviewerUsername,
                    "EMPLOYEE_DOCUMENT_IMPORTED",
                    "/admin/employees/" + employeeId + "/documents/import",
                    "SUCCESS",
                    "EMPLOYEE_DOCUMENT",
                    saved.getId(),
                    "documentType=" + documentType + ", validationStatus=" + saved.getValidationStatus() + ", conflictStatus=" + saved.getConflictStatus()
            );
            return saved;
        } catch (Exception ex) {
            auditTrailService.record(
                    reviewerUsername,
                    "EMPLOYEE_DOCUMENT_IMPORTED",
                    "/admin/employees/" + employeeId + "/documents/import",
                    "FAILURE",
                    "EMPLOYEE_DOCUMENT",
                    employeeId,
                    ex.getMessage()
            );
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    public List<EmployeeDocument> getEmployeeImports(Long employeeId) {
        return employeeDocumentRepository.findByEmployeeIdOrderByIdDesc(employeeId);
    }

    @Transactional
    public EmployeeDocument confirmImport(Long documentId,
                                          boolean overrideConflicts,
                                          String reviewerUsername) {
        EmployeeDocument document = employeeDocumentRepository.findById(documentId)
                .orElseThrow(() -> new IllegalArgumentException("Document not found"));
        Employee employee = employeeRepository.findById(document.getEmployeeId())
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        List<String> unappliedConflicts = new ArrayList<>();
        applyValue("name", employee.getName(), document.getExtractedName(), overrideConflicts, unappliedConflicts, employee::setName);
        applyValue("employeeCode", employee.getEmployeeCode(), document.getExtractedEmployeeCode(), overrideConflicts, unappliedConflicts, employee::setEmployeeCode);
        applyValue("panNumber", employee.getPanNumber(), document.getExtractedPanNumber(), overrideConflicts, unappliedConflicts, employee::setPanNumber);
        applyValue("aadhaarNumber", employee.getAadhaarNumber(), document.getExtractedAadhaarNumber(), overrideConflicts, unappliedConflicts, employee::setAadhaarNumber);
        applyValue("pfNumber", employee.getPfNumber(), document.getExtractedPfNumber(), overrideConflicts, unappliedConflicts, employee::setPfNumber);
        applyValue("uanNumber", employee.getUanNumber(), document.getExtractedUanNumber(), overrideConflicts, unappliedConflicts, employee::setUanNumber);
        applyValue("accountNumber", employee.getAccountNumber(), document.getExtractedAccountNumber(), overrideConflicts, unappliedConflicts, employee::setAccountNumber);
        applyValue("ifsc", employee.getIfsc(), document.getExtractedIfsc(), overrideConflicts, unappliedConflicts, employee::setIfsc);

        if (!unappliedConflicts.isEmpty()) {
            throw new IllegalStateException("Conflicts require explicit override: " + String.join(", ", unappliedConflicts));
        }

        employeeRepository.save(employee);
        document.setReviewStatus("CONFIRMED");
        document.setReviewedBy(reviewerUsername);
        document.setReviewedAt(LocalDateTime.now());
        document.setReviewNotes((document.getReviewNotes() == null ? "" : document.getReviewNotes() + " ")
                + "Confirmed by HR/Admin. Extracted email retained for manual identity review to avoid breaking authentication.");
        EmployeeDocument saved = employeeDocumentRepository.save(document);

        auditTrailService.record(
                reviewerUsername,
                "EMPLOYEE_DOCUMENT_CONFIRMED",
                "/admin/employees/documents/" + documentId + "/confirm",
                "SUCCESS",
                "EMPLOYEE_DOCUMENT",
                saved.getId(),
                "employeeId=" + employee.getEmpId() + ", overrideConflicts=" + overrideConflicts
        );

        return saved;
    }

    private void evaluateValidationAndConflicts(EmployeeDocument document, Employee employee) {
        List<String> validationIssues = new ArrayList<>();
        List<String> conflictIssues = new ArrayList<>();

        validatePattern(document.getExtractedPanNumber(), PAN_PATTERN, "PAN", validationIssues);
        validatePattern(normalizeNumeric(document.getExtractedAadhaarNumber()), Pattern.compile("[0-9]{12}"), "Aadhaar", validationIssues);
        validatePattern(document.getExtractedIfsc(), IFSC_PATTERN, "IFSC", validationIssues);
        validatePattern(document.getExtractedAccountNumber(), ACCOUNT_PATTERN, "Account", validationIssues);
        validatePattern(document.getExtractedUanNumber(), UAN_PATTERN, "UAN", validationIssues);

        compareConflict("name", employee.getName(), document.getExtractedName(), conflictIssues);
        compareConflict("employeeCode", employee.getEmployeeCode(), document.getExtractedEmployeeCode(), conflictIssues);
        compareConflict("panNumber", employee.getPanNumber(), document.getExtractedPanNumber(), conflictIssues);
        compareConflict("aadhaarNumber", employee.getAadhaarNumber(), document.getExtractedAadhaarNumber(), conflictIssues);
        compareConflict("pfNumber", employee.getPfNumber(), document.getExtractedPfNumber(), conflictIssues);
        compareConflict("uanNumber", employee.getUanNumber(), document.getExtractedUanNumber(), conflictIssues);
        compareConflict("accountNumber", employee.getAccountNumber(), document.getExtractedAccountNumber(), conflictIssues);
        compareConflict("ifsc", employee.getIfsc(), document.getExtractedIfsc(), conflictIssues);

        document.setExtractionStatus("EXTRACTED");
        document.setValidationStatus(validationIssues.isEmpty() ? "VALID" : "INVALID");
        document.setConflictStatus(conflictIssues.isEmpty() ? "NO_CONFLICT" : "CONFLICT");
        document.setReviewNotes(buildNotes(validationIssues, conflictIssues));
    }

    private String buildNotes(List<String> validationIssues, List<String> conflictIssues) {
        List<String> notes = new ArrayList<>();
        if (!validationIssues.isEmpty()) {
            notes.add("Validation: " + String.join("; ", validationIssues));
        }
        if (!conflictIssues.isEmpty()) {
            notes.add("Conflicts: " + String.join("; ", conflictIssues));
        }
        return notes.isEmpty() ? "Auto extraction completed successfully." : String.join(" | ", notes);
    }

    private void mapExtractedFields(EmployeeDocument document, String extractedText) {
        String normalized = extractedText.replace('\r', ' ').replace('\n', ' ');
        document.setExtractedEmail(firstMatch(normalized, EMAIL_PATTERN));
        document.setExtractedPanNumber(firstMatch(normalized.toUpperCase(Locale.ROOT), PAN_PATTERN));
        document.setExtractedAadhaarNumber(normalizeNumeric(firstMatch(normalized, AADHAAR_PATTERN)));
        document.setExtractedIfsc(firstMatch(normalized.toUpperCase(Locale.ROOT), IFSC_PATTERN));
        document.setExtractedAccountNumber(firstMatch(normalized, ACCOUNT_PATTERN));
        document.setExtractedUanNumber(firstMatch(normalized, UAN_PATTERN));
        document.setExtractedEmployeeCode(firstGroup(normalized, EMPLOYEE_CODE_PATTERN));
        document.setExtractedPfNumber(firstGroup(normalized, PF_PATTERN));
        document.setExtractedName(extractName(normalized));
    }

    private String extractText(String originalFileName, byte[] bytes) {
        String lowerName = originalFileName.toLowerCase(Locale.ROOT);
        try {
            if (lowerName.endsWith(".pdf")) {
                PdfReader reader = new PdfReader(bytes);
                StringBuilder content = new StringBuilder();
                for (int page = 1; page <= reader.getNumberOfPages(); page++) {
                    content.append(PdfTextExtractor.getTextFromPage(reader, page)).append('\n');
                }
                reader.close();
                return content.toString();
            }
            if (lowerName.endsWith(".txt") || lowerName.endsWith(".csv")) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
            return "";
        } catch (Exception e) {
            return "";
        }
    }

    private String extractName(String text) {
        for (String token : text.split(" {2,}|\\|")) {
            String candidate = token.trim();
            if (candidate.toLowerCase(Locale.ROOT).startsWith("name")) {
                String[] parts = candidate.split("[:-]", 2);
                if (parts.length == 2 && !parts[1].isBlank()) {
                    return parts[1].trim();
                }
            }
        }
        return null;
    }

    private String firstMatch(String source, Pattern pattern) {
        if (source == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(source);
        return matcher.find() ? matcher.group().trim() : null;
    }

    private String firstGroup(String source, Pattern pattern) {
        if (source == null) {
            return null;
        }
        Matcher matcher = pattern.matcher(source);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    private void compareConflict(String field, String existingValue, String extractedValue, List<String> issues) {
        if (isBlank(existingValue) || isBlank(extractedValue)) {
            return;
        }
        if (!existingValue.trim().equalsIgnoreCase(extractedValue.trim())) {
            issues.add(field + " differs from employee master");
        }
    }

    private void validatePattern(String value, Pattern pattern, String label, List<String> issues) {
        if (isBlank(value)) {
            return;
        }
        if (!pattern.matcher(value).matches()) {
            issues.add(label + " format mismatch");
        }
    }

    private void applyValue(String fieldName,
                            String currentValue,
                            String extractedValue,
                            boolean overrideConflicts,
                            List<String> unappliedConflicts,
                            java.util.function.Consumer<String> setter) {
        if (isBlank(extractedValue)) {
            return;
        }
        if (!isBlank(currentValue) && !currentValue.trim().equalsIgnoreCase(extractedValue.trim()) && !overrideConflicts) {
            unappliedConflicts.add(fieldName);
            return;
        }
        setter.accept(extractedValue.trim());
    }

    private String sha256(byte[] bytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String normalizeNumeric(String value) {
        return value == null ? null : value.replaceAll("\\D", "");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
