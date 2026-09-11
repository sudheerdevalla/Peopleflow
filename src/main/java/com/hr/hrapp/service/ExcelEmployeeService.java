package com.hr.hrapp.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.hr.hrapp.entity.Employee;
import com.hr.hrapp.repository.EmployeeRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.hr.hrapp.entity.User;
import com.hr.hrapp.repository.UserRepository;

@Service
public class ExcelEmployeeService {

    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;
    @Transactional(rollbackFor = Exception.class)
    public void importEmployees(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select an Excel file.");
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {

            if (workbook.getNumberOfSheets() == 0) {
                throw new IllegalArgumentException(
                        "Excel file does not contain any sheet.");
            }

            Sheet sheet = workbook.getSheetAt(0);

            if (sheet.getPhysicalNumberOfRows() < 2) {
                throw new IllegalArgumentException(
                        "Excel file must contain a header row and at least one employee row.");
            }

            Row headerRow = sheet.getRow(0);

            if (headerRow == null) {
                throw new IllegalArgumentException(
                        "Excel header row is missing.");
            }

            Map<String, Integer> columns = buildHeaderMap(headerRow);

            validateRequiredHeaders(columns);

            DataFormatter formatter = new DataFormatter();

            Set<String> employeeCodesInFile = new HashSet<>();
            Set<String> emailsInFile = new HashSet<>();

            for (int rowIndex = 1;
                    rowIndex <= sheet.getLastRowNum();
                    rowIndex++) {

                Row row = sheet.getRow(rowIndex);

                if (row == null || isEmptyRow(row, formatter)) {
                    continue;
                }

                int excelRowNumber = rowIndex + 1;

                Employee emp = new Employee();

                String employeeCode =
                        getCellValue(
                                row,
                                columns,
                                "employee id",
                                formatter);

                String firstName =
                        getCellValue(
                                row,
                                columns,
                                "first name",
                                formatter);

                String lastName =
                        getCellValue(
                                row,
                                columns,
                                "last name",
                                formatter);

                String email =
                        getCellValue(
                                row,
                                columns,
                                "company email",
                                formatter);

                if (employeeCode.isBlank()) {
                    throw new IllegalArgumentException(
                            "Row " + excelRowNumber
                                    + ": Employee ID is required.");
                }

                if (firstName.isBlank()) {
                    throw new IllegalArgumentException(
                            "Row " + excelRowNumber
                                    + ": First Name is required.");
                }

                if (email.isBlank()) {
                    throw new IllegalArgumentException(
                            "Row " + excelRowNumber
                                    + ": Company Email is required.");
                }

                employeeCode = employeeCode.trim();
                email = email.trim();

                if (employeeRepository
                        .findByEmployeeCode(employeeCode)
                        .isPresent()) {

                    throw new IllegalArgumentException(
                            "Employee ID already exists in database: "
                                    + employeeCode);
                }

                if (!employeeCodesInFile.add(employeeCode)) {

                    throw new IllegalArgumentException(
                            "Duplicate Employee ID in Excel: "
                                    + employeeCode
                                    + " at row "
                                    + excelRowNumber);
                }

                if (employeeRepository.findByEmail(email) != null) {

                    throw new IllegalArgumentException(
                            "Company Email already exists in database: "
                                    + email);
                }

                if (!emailsInFile.add(email.toLowerCase())) {

                    throw new IllegalArgumentException(
                            "Duplicate Company Email in Excel: "
                                    + email
                                    + " at row "
                                    + excelRowNumber);
                }

                /*
                 * Employee ID from Excel maps to employeeCode.
                 * Do not use database empId here.
                 */

                emp.setEmployeeCode(employeeCode);

                emp.setFirstName(firstName);

                emp.setLastName(lastName);

                String fullName =
                        buildFullName(firstName, lastName);

                emp.setName(fullName);

                emp.setEmail(email);

                emp.setPersonalEmail(null);

                emp.setMobile(
                        getCellValue(
                                row,
                                columns,
                                "phone",
                                formatter));

                emp.setGender(
                        getCellValue(
                                row,
                                columns,
                                "gender",
                                formatter));

                String department =
                        getCellValue(
                                row,
                                columns,
                                "department",
                                formatter);

                if (!department.equalsIgnoreCase("Solar & OM")) {

                    throw new IllegalArgumentException(
                            "Row " + excelRowNumber
                                    + ": Invalid Department. "
                                    + "Allowed value: Solar & OM");
                }

                emp.setDepartment(department);

                String designation =
                        getCellValue(
                                row,
                                columns,
                                "designation",
                                formatter);

                if (!(designation.equalsIgnoreCase("Managing Director")
                        || designation.equalsIgnoreCase("Director")
                        || designation.equalsIgnoreCase("Lead Engineer")
                        || designation.equalsIgnoreCase("Engineer")
                        || designation.equalsIgnoreCase("Sr Technician")
                        || designation.equalsIgnoreCase("Technician"))) {

                    throw new IllegalArgumentException(
                            "Row " + excelRowNumber
                                    + ": Invalid Designation.");
                }

                emp.setDesignation(designation);

                String employmentType =
                        getCellValue(
                                row,
                                columns,
                                "employment type",
                                formatter);

                if (!(employmentType.equalsIgnoreCase("Full-Time")
                        || employmentType.equalsIgnoreCase("Part-Time")
                        || employmentType.equalsIgnoreCase("Contract")
                        || employmentType.equalsIgnoreCase("Intern"))) {

                    throw new IllegalArgumentException(
                            "Row " + excelRowNumber
                                    + ": Invalid Employment Type.");
                }

                emp.setEmploymentType(employmentType);

                String totalExperience =
                        getCellValue(
                                row,
                                columns,
                                "total experience (years)",
                                formatter);

                if (!totalExperience.isBlank()) {
                    emp.setExperience(totalExperience);
                }

                String location =
                        getCellValue(
                                row,
                                columns,
                                "work location",
                                formatter);

                if (!(location.equalsIgnoreCase("Hyderabad")
                        || location.equalsIgnoreCase("Visakhapatnam"))) {

                    throw new IllegalArgumentException(
                            "Row " + excelRowNumber
                                    + ": Invalid Work Location. "
                                    + "Allowed values: Hyderabad, Visakhapatnam");
                }

                emp.setLocation(location);

                String managerEmployeeCode =
                        getCellValue(
                                row,
                                columns,
                                "manager employee id",
                                formatter);

                if (!managerEmployeeCode.isBlank()) {

                    Employee manager =
                            employeeRepository
                                    .findByEmployeeCode(
                                            managerEmployeeCode.trim())
                                    .orElseThrow(
                                            () -> new IllegalArgumentException(
                                                    "Row "
                                                            + excelRowNumber
                                                            + ": Manager Employee ID not found: "
                                                            + managerEmployeeCode));

                    emp.setManager(manager);
                }

                emp.setAddress(
                        getCellValue(
                                row,
                                columns,
                                "address",
                                formatter));

                emp.setCity(
                        getCellValue(
                                row,
                                columns,
                                "city",
                                formatter));

                emp.setState(
                        getCellValue(
                                row,
                                columns,
                                "state",
                                formatter));

                emp.setPincode(
                        getCellValue(
                                row,
                                columns,
                                "pincode",
                                formatter));

                emp.setEmergencyContactName(
                        getCellValue(
                                row,
                                columns,
                                "emergency contact name",
                                formatter));

                emp.setEmergencyContactPhone(
                        getCellValue(
                                row,
                                columns,
                                "emergency contact phone",
                                formatter));

                emp.setHighestQualification(
                        getCellValue(
                                row,
                                columns,
                                "highest qualification",
                                formatter));

                emp.setPreviousCompany(
                        getCellValue(
                                row,
                                columns,
                                "previous company",
                                formatter));

                String onboardingStatus =
                        getCellValue(
                                row,
                                columns,
                                "onboarding status",
                                formatter);

                if (!onboardingStatus.equalsIgnoreCase("Completed")) {

                    throw new IllegalArgumentException(
                            "Row " + excelRowNumber
                                    + ": Invalid Onboarding Status. "
                                    + "Allowed value: Completed");
                }

                emp.setOnboardingStatus(onboardingStatus);

                /*
                 * Date of Birth
                 */

                emp.setDateOfBirth(
                        getDateValue(
                                row,
                                columns,
                                "date of birth",
                                formatter));

                /*
                 * Date of Joining
                 */

                emp.setJoiningDate(
                        getDateValue(
                                row,
                                columns,
                                "date of joining",
                                formatter));

                /*
                 * Annual Salary (CTC) -> totalCtc
                 */

                String annualSalary =
                        getCellValue(
                                row,
                                columns,
                                "annual salary (ctc)",
                                formatter);

                if (annualSalary.isBlank()) {
                    annualSalary =
                            getCellValue(
                                    row,
                                    columns,
                                    "annual salary",
                                    formatter);
                }

                if (!annualSalary.isBlank()) {

                    emp.setTotalCtc(
                            parseDouble(
                                    annualSalary,
                                    "Annual Salary (CTC)",
                                    excelRowNumber));
                }

                /*
                 * Optional existing employee information.
                 */

                emp.setBankName(
                        getCellValue(
                                row,
                                columns,
                                "bank name",
                                formatter));

                emp.setAccountNumber(
                        getCellValue(
                                row,
                                columns,
                                "account number",
                                formatter));

                emp.setIfsc(
                        getCellValue(
                                row,
                                columns,
                                "ifsc",
                                formatter));

                emp.setPanNumber(
                        getCellValue(
                                row,
                                columns,
                                "pan",
                                formatter));

                emp.setAadhaarNumber(
                        getCellValue(
                                row,
                                columns,
                                "aadhaar",
                                formatter));

                emp.setUanNumber(
                        getCellValue(
                                row,
                                columns,
                                "uan",
                                formatter));

                emp.setPfNumber(
                        getCellValue(
                                row,
                                columns,
                                "pf number",
                                formatter));

                emp.setEsiNumber(
                        getCellValue(
                                row,
                                columns,
                                "esi number",
                                formatter));

                /*
                 * Preserve current application defaults.
                 */

                emp.setStatus("Active");
                emp.setRole("USER");

                /*
                 * Save employee after complete validation.
                 */

                employeeRepository.save(emp);

                /*
                 * Generate a random temporary password.
                 * Only the encoded password is stored in DB.
                 */

                String temporaryPassword =
                        java.util.UUID.randomUUID()
                                .toString()
                                .replace("-", "")
                                .substring(0, 10);

                User user = new User();

                user.setUsername(emp.getEmail());

                user.setPassword(
                        passwordEncoder.encode(temporaryPassword));

                user.setRole("USER");

                user.setForcePasswordChange(true);

                userRepository.save(user);

                /*
                 * Send welcome email to this employee's own
                 * company email with this employee's own
                 * temporary password.
                 */

                try {

                    emailService.sendWelcomeMail(
                            emp.getEmail(),
                            emp.getName(),
                            temporaryPassword);

                } catch (Exception e) {

                    e.printStackTrace();
                }
            }

        } catch (IllegalArgumentException e) {

            throw e;

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Failed to import employees from Excel: "
                            + e.getMessage(),
                    e);
        }
    }

    private Map<String, Integer> buildHeaderMap(Row headerRow) {

        Map<String, Integer> columns = new HashMap<>();

        DataFormatter formatter = new DataFormatter();

        for (Cell cell : headerRow) {

            String header =
                    formatter.formatCellValue(cell)
                            .trim()
                            .toLowerCase();

            if (!header.isBlank()) {
                columns.put(header, cell.getColumnIndex());
            }
        }

        return columns;
    }

    private void validateRequiredHeaders(
            Map<String, Integer> columns) {

        String[] requiredHeaders = {
                "employee id",
                "first name",
                "last name",
                "date of birth",
                "gender",
                //"personal email",
                "phone",
                "company email",
                "date of joining",
                "department",
                "designation",
                "employment type",
                "work location",
                "address",
                "city",
                "state",
                "pincode",
                "emergency contact name",
                "highest qualification",
                "previous company",
                "annual salary (ctc)",
                "onboarding status"
        };

        for (String header : requiredHeaders) {

            if (!columns.containsKey(header)) {

                throw new IllegalArgumentException(
                        "Required Excel column is missing: "
                                + header);
            }
        }
    }

    private String getCellValue(
            Row row,
            Map<String, Integer> columns,
            String header,
            DataFormatter formatter) {

        Integer columnIndex = columns.get(header.toLowerCase());

        if (columnIndex == null) {
            return "";
        }

        Cell cell = row.getCell(
                columnIndex,
                Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

        if (cell == null) {
            return "";
        }

        return formatter.formatCellValue(cell).trim();
    }

    private LocalDate getDateValue(
            Row row,
            Map<String, Integer> columns,
            String header,
            DataFormatter formatter) {

        Integer columnIndex =
                columns.get(header.toLowerCase());

        if (columnIndex == null) {
            return null;
        }

        Cell cell =
                row.getCell(
                        columnIndex,
                        Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);

        if (cell == null) {
            return null;
        }

        if (cell.getCellType() == CellType.NUMERIC
                && DateUtil.isCellDateFormatted(cell)) {

            return cell.getDateCellValue()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }

        String value =
                formatter.formatCellValue(cell).trim();

        if (value.isBlank()) {
            return null;
        }

        String[] formats = {
                "dd-MM-yyyy",
                "dd/MM/yyyy",
                "yyyy-MM-dd",
                "dd-MMM-yyyy",
                "d-MMM-yyyy"
        };

        for (String format : formats) {

            try {

                return LocalDate.parse(
                        value,
                        java.time.format.DateTimeFormatter
                                .ofPattern(format));

            } catch (Exception ignored) {
            }
        }

        throw new IllegalArgumentException(
                "Invalid "
                        + header
                        + " value: "
                        + value);
    }

    private double parseDouble(
            String value,
            String fieldName,
            int rowNumber) {

        try {

            String cleaned =
                    value.replace(",", "")
                            .replace("₹", "")
                            .trim();

            return Double.parseDouble(cleaned);

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Row "
                            + rowNumber
                            + ": Invalid "
                            + fieldName
                            + " value: "
                            + value);
        }
    }

    private String buildFullName(
            String firstName,
            String lastName) {

        if (lastName == null || lastName.isBlank()) {
            return firstName.trim();
        }

        return (firstName.trim()
                + " "
                + lastName.trim()).trim();
    }

    private boolean isEmptyRow(
            Row row,
            DataFormatter formatter) {

        for (Cell cell : row) {

            if (cell != null
                    && !formatter
                            .formatCellValue(cell)
                            .trim()
                            .isBlank()) {

                return false;
            }
        }

        return true;
    }
}