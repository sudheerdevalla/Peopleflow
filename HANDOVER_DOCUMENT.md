# PeopleFlow HRMS - Professional Handover Document

**Document Version:** 1.0  
**Last Updated:** June 25, 2026  
**Project Status:** Production Ready  

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Features Implemented](#features-implemented)
3. [Technology Stack](#technology-stack)
4. [Database Tables](#database-tables)
5. [Folder Structure](#folder-structure)
6. [Security Architecture](#security-architecture)
7. [Email Configuration](#email-configuration)
8. [Payroll Processing Flow](#payroll-processing-flow)
9. [Recruitment Workflow](#recruitment-workflow)
10. [Deployment Instructions](#deployment-instructions)
11. [API Endpoints](#api-endpoints)
12. [Configuration Guide](#configuration-guide)
13. [Known Limitations](#known-limitations)
14. [Troubleshooting Guide](#troubleshooting-guide)
15. [Future Enhancements](#future-enhancements)

---

## 1. Project Overview

### Project Name
**PeopleFlow** - An Enterprise-Grade Human Resource Management System

### Project Description
PeopleFlow is a comprehensive, production-ready HRMS application designed to streamline human resource operations, including employee lifecycle management, attendance tracking, payroll processing, leave management, and talent acquisition workflows. The system is built with modern Spring Boot technologies and follows enterprise security best practices.

### Key Highlights
- ✅ End-to-end HRMS functionality
- ✅ Multi-role based access control
- ✅ JWT-based authentication
- ✅ Real-time attendance with GPS validation
- ✅ Automated payroll processing with PDF generation
- ✅ Email notifications for critical events
- ✅ Comprehensive audit logging
- ✅ HTTPS/SSL enabled for data security

### Project Metadata
| Property | Value |
|----------|-------|
| **Application Name** | PeopleFlow HRMS |
| **Version** | 0.0.1-SNAPSHOT |
| **Group ID** | com.hr |
| **Artifact ID** | hrapp |
| **Database** | MySQL (peopleflow schema) |
| **Port** | 8443 (HTTPS) |
| **Build Tool** | Maven |

---

## 2. Features Implemented

### 2.1 Authentication & Authorization
- ✅ JWT-based token authentication
- ✅ User login/logout functionality
- ✅ Password encryption using BCrypt
- ✅ Role-based access control (RBAC)
- ✅ Permission-level granular access
- ✅ HTTPS/SSL encryption for all transactions
- ✅ Keystore-based SSL certificate management

### 2.2 Employee Management
- ✅ Employee profile creation and updates
- ✅ Document upload functionality
- ✅ Employee status tracking
- ✅ Department and location management
- ✅ Employee hierarchy and reporting
- ✅ Bulk employee operations

### 2.3 Dashboard
- ✅ Executive dashboard with key metrics
- ✅ HR analytics and reporting
- ✅ Real-time data visualization
- ✅ Custom report generation
- ✅ Role-specific dashboard views

### 2.4 Attendance Management
- ✅ Check-in/Check-out functionality
- ✅ GPS-based location validation (5km radius)
- ✅ Attendance reports and analytics
- ✅ Manual attendance entry
- ✅ Late arrival and early departure tracking
- ✅ Attendance penalization logic

### 2.5 Timesheet Management
- ✅ Daily timesheet entry
- ✅ Project-wise time allocation
- ✅ Overtime calculation
- ✅ Timesheet approval workflow
- ✅ Manager-level timesheet validation
- ✅ Penalty calculation for timesheet violations

### 2.6 Leave Management
- ✅ Multiple leave types (Sick, Casual, Earned, etc.)
- ✅ Leave request workflow
- ✅ Manager approval system
- ✅ Leave balance tracking
- ✅ Holiday calendar management
- ✅ Leave rejection with comments

### 2.7 Travel Requests
- ✅ Travel request creation
- ✅ Multi-level approval workflow
- ✅ Travel audit trail
- ✅ Travel expense management
- ✅ Travel history tracking

### 2.8 Recruitment Module
- ✅ Candidate application management
- ✅ Candidate shortlisting
- ✅ Interview scheduling
- ✅ Selection notification
- ✅ Offer letter generation
- ✅ Multi-stage recruitment workflow

### 2.9 Resume Management
- ✅ Resume upload and storage
- ✅ Resume preview functionality
- ✅ Resume archive management
- ✅ Document tagging system

### 2.10 Offer Letter Generation
- ✅ Automated offer letter generation
- ✅ Customizable letter templates
- ✅ PDF generation and email delivery
- ✅ Offer tracking and acceptance workflow

### 2.11 Payroll Processing
- ✅ Salary structure definition
- ✅ Salary calculation (Basic, HRA, DA, etc.)
- ✅ Deduction calculation (Tax, PF, etc.)
- ✅ Payslip PDF generation using iText
- ✅ Salary advance processing
- ✅ Monthly payroll execution
- ✅ Payslip email distribution

### 2.12 Financial Module
- ✅ Expense management
- ✅ Budget tracking
- ✅ Financial reports
- ✅ Award management
- ✅ Financial audit trail

### 2.13 Email Notifications
- ✅ Offer letter delivery
- ✅ Payslip distribution
- ✅ Leave approval notifications
- ✅ Travel request updates
- ✅ System alerts and reminders
- ✅ SMTP configuration via Gmail

### 2.14 Role & Permission Management
- ✅ Dynamic role creation
- ✅ Permission assignment to roles
- ✅ Role hierarchy management
- ✅ Multi-role user support
- ✅ Permission-based feature access

### 2.15 Audit & Compliance
- ✅ Comprehensive audit logging
- ✅ Change tracking for all entities
- ✅ User activity monitoring
- ✅ Compliance reporting

---

## 3. Technology Stack

| Layer | Technology | Version |
|-------|-----------|---------|
| **Java Version** | Java Development Kit (JDK) | 17+ |
| **Framework** | Spring Boot | 4.0.5 |
| **Security** | Spring Security | Latest |
| **Authentication** | JWT (JSON Web Token) | 0.11.5 |
| **ORM** | Hibernate / JPA | Spring Boot Default |
| **Template Engine** | Thymeleaf | Latest |
| **Database** | MySQL | 8.0+ |
| **Build Tool** | Maven | 3.8+ |
| **Frontend** | Bootstrap | Latest |
| **Styling** | HTML/CSS/JavaScript | ES6+ |
| **PDF Generation** | iText | 5.5.13.3 |
| **Email** | JavaMail (Spring Mail) | Latest |
| **Excel Export** | Apache POI | 5.2.5 |
| **OpenAPI** | SpringDoc OpenAPI | 2.5.0 |
| **Cryptography** | BouncyCastle | 1.70 |
| **Database Driver** | MySQL Connector/J | Latest |
| **Validation** | Spring Validation | Latest |

### Additional Libraries
- **Testing:** JUnit 5, Mockito
- **Logging:** SLF4J with Logback
- **JSON Processing:** Jackson

---

## 4. Database Tables

### Entity Relationship Overview

```
User (Authentication)
  ├── Role (Role-Based Access Control)
  │   └── Permission
  ├── Employee (Employee Information)
  │   ├── Salary (Payroll Information)
  │   ├── EmployeeAttendance (Attendance Records)
  │   ├── EmployeeDocument (Document Storage)
  │   ├── Timesheet (Work Time Tracking)
  │   │   └── TimesheetPenalty
  │   └── Leave (Leave Requests)
  ├── Candidate (Recruitment)
  ├── TravelRequest (Travel Management)
  │   └── TravelAudit (Travel Audit Trail)
  ├── Notification
  ├── AuditLog (System Audit)
  ├── Holiday (Holiday Calendar)
  ├── Location (Office Locations)
  ├── CompanyUpdate (Company News)
  └── Permission (Dynamic Permissions)
```

### Major Entities and Their Purpose

| Entity | Purpose | Key Fields |
|--------|---------|-----------|
| **User** | System authentication and identification | userId, username, password, email, isActive |
| **Employee** | Core employee information | employeeId, firstName, lastName, email, department, designation, joinDate |
| **Role** | User role definition | roleId, roleName, description |
| **Permission** | Fine-grained access control | permissionId, permissionName, description |
| **Salary** | Payroll and salary information | salaryId, employeeId, basicSalary, hra, da, deductions, netSalary |
| **EmployeeAttendance** | Daily attendance tracking | attendanceId, employeeId, checkInTime, checkOutTime, latitude, longitude |
| **Timesheet** | Project time tracking | timesheetId, employeeId, projectId, hours, date, status |
| **TimesheetPenalty** | Timesheet violation penalties | penaltyId, timesheetId, penaltyAmount, reason |
| **Leave** | Leave request workflow | leaveId, employeeId, leaveType, startDate, endDate, status, approvalDate |
| **Candidate** | Recruitment pipeline | candidateId, name, email, phone, resumePath, status, appliedDate |
| **TravelRequest** | Travel authorization | travelId, employeeId, destination, startDate, endDate, purpose, status |
| **TravelAudit** | Travel request audit trail | auditId, travelId, action, timestamp, changedBy |
| **Holiday** | Company holidays | holidayId, holidayName, holidayDate |
| **Location** | Office locations | locationId, locationName, address, city, country |
| **Notification** | System notifications | notificationId, userId, message, isRead, createdDate |
| **AuditLog** | System-wide audit trail | auditId, entityName, action, userId, timestamp, changes |
| **EmployeeDocument** | Employee document storage | documentId, employeeId, documentName, filePath, uploadDate |
| **CompanyUpdate** | Company announcements | updateId, title, description, publishedDate |

---

## 5. Folder Structure

### Project Layout: `src/main/java/com/hr/hrapp/`

```
com/hr/hrapp/
├── HrappApplication.java          # Main Spring Boot Application Entry Point
├── controller/                    # REST API Controllers
│   ├── AuthController.java        # Authentication endpoints
│   ├── JwtAuthController.java     # JWT token management
│   ├── EmployeeController.java    # Employee CRUD operations
│   ├── FinancialController.java   # Financial module endpoints
│   ├── CandidateController.java   # Recruitment management
│   ├── TravelController.java      # Travel request operations
│   ├── RoleController.java        # Role management
│   ├── PermissionController.java  # Permission management
│   ├── AuditLogController.java    # Audit log retrieval
│   ├── HolidayController.java     # Holiday management
│   ├── UserController.java        # User management
│   ├── LocationController.java    # Location management
│   ├── RecruitmentDashboardController.java # Recruitment stats
│   ├── AdminCompanyUpdateController.java   # Company announcements
│   └── TestController.java        # Testing endpoints
├── entity/                        # JPA/Hibernate Entities
│   ├── User.java                  # User entity
│   ├── Employee.java              # Employee entity
│   ├── Role.java                  # Role entity
│   ├── Permission.java            # Permission entity
│   ├── Salary.java                # Salary entity
│   ├── EmployeeAttendance.java    # Attendance entity
│   ├── Timesheet.java             # Timesheet entity
│   ├── TimesheetPenalty.java      # Timesheet penalty entity
│   ├── Leave.java                 # Leave entity
│   ├── Candidate.java             # Candidate entity
│   ├── TravelRequest.java         # Travel request entity
│   ├── TravelAudit.java           # Travel audit entity
│   ├── Holiday.java               # Holiday entity
│   ├── Location.java              # Location entity
│   ├── Notification.java          # Notification entity
│   ├── AuditLog.java              # Audit log entity
│   ├── EmployeeDocument.java      # Document entity
│   └── CompanyUpdate.java         # Company update entity
├── repository/                    # Spring Data JPA Repositories
├── service/                       # Business Logic Services
├── security/                      # Security Configuration
│   ├── JwtUtil.java               # JWT token utilities
│   ├── JwtAuthenticationFilter.java # JWT request filter
│   ├── SecurityConfig.java        # Spring Security configuration
│   └── CustomUserDetailsService.java # Custom user details
├── config/                        # Application Configuration
├── dto/                           # Data Transfer Objects
├── scheduler/                     # Scheduled Tasks
│   ├── PayrollScheduler.java      # Monthly payroll execution
│   ├── NotificationScheduler.java # Notification sending
│   └── ReportScheduler.java       # Report generation
├── audit/                         # Audit Components
└── exception/                     # Exception Handling
```

### Resources Directory: `src/main/resources/`

```
resources/
├── application.properties        # Main configuration file
├── application-dev.properties   # Development profile
├── application-prod.properties  # Production profile
├── keystore/
│   └── peopleflow.p12           # SSL Certificate (PKCS12)
├── db/
│   └── migrations/
│       └── V2__add_indexes_and_constraints.sql
├── static/                      # CSS, JS, Images
└── templates/                   # Thymeleaf HTML templates
```

---

## 6. Security Architecture

### 6.1 Spring Security Configuration

#### Overview
PeopleFlow implements a comprehensive security framework with Spring Security 4.0.5, providing:
- Multi-layer authentication and authorization
- JWT-based stateless authentication
- Method-level security
- HTTPS/SSL encryption
- CORS protection

### 6.2 JWT Authentication Details

| Property | Value |
|----------|-------|
| **JWT Library** | JJWT (Java JWT) v0.11.5 |
| **Token Type** | Bearer Token |
| **Secret Key** | Stored in application.properties |
| **Expiration** | 10 hours (36000000 milliseconds) |
| **Algorithm** | HS512 (HMAC with SHA-512) |

### 6.3 Password Encryption

| Feature | Implementation |
|---------|-----------------|
| **Algorithm** | BCrypt (Spring Security Default) |
| **Work Factor** | 10 (configurable) |
| **Strength** | Military-grade encryption |

### 6.4 Built-in Roles

| Role | Description | Permissions |
|------|-------------|-------------|
| **ADMIN** | System administrator | All permissions |
| **HR_MANAGER** | HR department management | Employee CRUD, Payroll, Leave Approval |
| **MANAGER** | Department manager | Team management, Timesheet approval |
| **EMPLOYEE** | Regular employee | View own profile, Apply leave, Mark attendance |
| **RECRUITER** | Recruitment team | Candidate management, Interview scheduling |
| **FINANCE** | Financial operations | Payroll view, Financial reports |

### 6.5 HTTPS/SSL Configuration

```properties
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore/peopleflow.p12
server.ssl.key-store-password=peopleflow123
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=peopleflow
```

### 6.6 Security Best Practices Implemented

✅ Input Validation - All inputs validated using Spring Validation  
✅ SQL Injection Prevention - Parameterized queries via JPA  
✅ XSS Protection - Output encoding in Thymeleaf templates  
✅ CSRF Protection - Spring Security CSRF tokens  
✅ Password Security - BCrypt hashing with salt  
✅ Session Management - Stateless JWT authentication  
✅ Audit Logging - All sensitive operations logged  
✅ Data Encryption - HTTPS/SSL for all communications  

---

## 7. Email Configuration

### 7.1 SMTP Configuration

#### Email Provider: Gmail
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=sudheerdevalla950214@gmail.com
spring.mail.password=oxnpfnlozontjvsi (App Password)
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### 7.2 Email Templates

#### Offer Letter Email
- **Recipient:** Candidate email
- **Subject:** "Congratulations! Offer Letter - PeopleFlow HRMS"
- **Content:** Job designation, Salary offer, Joining date, PDF attachment

#### Payslip Email
- **Recipient:** Employee email
- **Subject:** "Your Salary Payslip - Month Year"
- **Content:** Salary details, Deductions, PDF attachment
- **Frequency:** Monthly (automated via scheduler)

#### Leave Approval Notification
- **Recipient:** Employee + Manager
- **Subject:** "Leave Request - {Status}"
- **Content:** Leave period, Approval status, Comments

#### Travel Request Notification
- **Recipient:** Employee + Approvers
- **Subject:** "Travel Request - Awaiting Approval"
- **Content:** Destination, Travel dates, Purpose, Status

---

## 8. Payroll Processing Flow

### 8.1 Payroll Architecture Overview

```
Monthly Payroll Execution
    ↓
PayrollScheduler (triggered on 1st of month)
    ↓
PayrollService processes all active employees
    ↓
For each Employee:
    ├─ Calculate gross salary
    ├─ Calculate deductions
    ├─ Calculate net salary
    └─ Generate payslip
    ↓
PDFService generates payslip PDF
    ↓
EmailService sends to employee
    ↓
Payroll Complete - Status Updated
```

### 8.2 Salary Calculation Formula

#### Gross Salary Calculation
```
GROSS SALARY = BASIC + HRA + DA + CONVEYANCE + MEDICAL_ALLOWANCE

Example:
BASIC                          = ₹50,000
HRA (15% of Basic)             = ₹7,500
DA (10% of Basic)              = ₹5,000
CONVEYANCE                     = ₹1,500
GROSS SALARY                   = ₹64,000
```

#### Deductions Calculation
```
TOTAL_DEDUCTIONS = PF + PROFESSIONAL_TAX + INCOME_TAX + INSURANCE

PF (Provident Fund - 12%)      = ₹6,000
PROFESSIONAL_TAX               = ₹150
INCOME_TAX (as per slab)       = ₹5,000
TOTAL_DEDUCTIONS               = ₹11,150
```

#### Net Salary
```
NET SALARY = GROSS SALARY - TOTAL_DEDUCTIONS
NET SALARY = ₹64,000 - ₹11,150 = ₹52,850
```

### 8.3 Payroll Processing Steps

#### Step 1: Data Compilation
- Retrieve all active employees
- Collect salary data for each employee

#### Step 2: Salary Calculation Service
- Get attendance days worked
- Calculate gross salary
- Calculate deductions
- Calculate net salary

#### Step 3: Payslip Generation
- Create Payslip entity
- Store in database
- Update status

#### Step 4: PDF Generation
- Use iText for PDF creation
- Add company branding
- Embed salary details
- Save to filesystem

#### Step 5: Email Distribution
- Send payslip to employee
- Attach PDF
- Log delivery status

### 8.4 Payroll Scheduler Configuration

```properties
# PayrollScheduler triggers on 1st of every month at 00:00 AM
@Scheduled(cron = "0 0 0 1 * *")
public void executeMonthlyPayroll() {
    // Payroll execution logic
}
```

---

## 9. Recruitment Workflow

### 9.1 Recruitment Pipeline

```
Stage 1: APPLIED
    ├─ Candidate submits application
    ├─ Resume uploaded
    └─ Email confirmation sent

Stage 2: SHORTLISTED
    ├─ HR reviews resume
    ├─ Shortlisting decision made
    └─ Email notification sent

Stage 3: INTERVIEW_SCHEDULED
    ├─ Interview date assigned
    ├─ Interview panel selected
    └─ Email reminder sent

Stage 4: SELECTED
    ├─ Interview completed
    ├─ Candidate selected
    └─ Selection notification sent

Stage 5: OFFER_SENT
    ├─ Offer letter generated
    ├─ Sent via email with PDF
    └─ Candidate acceptance tracked

Stage 6: JOINED (Optional)
    ├─ Employee onboarding begins
    ├─ Employee record created
    └─ System access granted
```

### 9.2 Recruitment Status Transitions

| Current Status | Next Status | Action Required | Notification |
|---|---|---|---|
| **APPLIED** | SHORTLISTED | HR Review | Email to candidate |
| **SHORTLISTED** | INTERVIEW_SCHEDULED | Schedule Interview | Interview details |
| **INTERVIEW_SCHEDULED** | SELECTED | Post-Interview evaluation | Selection email |
| **SELECTED** | OFFER_SENT | Generate offer letter | Offer letter email |
| **OFFER_SENT** | JOINED | Offer acceptance | Onboarding email |

### 9.3 Recruitment Controllers & Services

#### CandidateController.java
```
POST   /api/candidates/apply              - Submit new application
GET    /api/candidates/{id}               - Get candidate details
PUT    /api/candidates/{id}/shortlist     - Shortlist candidate
PUT    /api/candidates/{id}/interview     - Schedule interview
PUT    /api/candidates/{id}/select        - Mark as selected
POST   /api/candidates/{id}/offer         - Send offer letter
```

### 9.4 Offer Letter Generation

- HR clicks "Generate Offer Letter"
- OfferLetter entity created with candidate details
- PDFService generates professional PDF
- Email sent to candidate with PDF attachment
- Candidate accepts/rejects offer
- System updated with status

---

## 10. Deployment Instructions

### 10.1 Prerequisites

#### System Requirements
| Component | Requirement | Version |
|-----------|-------------|---------|
| **Operating System** | Linux, Windows, or macOS | Any modern OS |
| **Java Development Kit** | JDK or OpenJDK | 17 or higher |
| **Maven** | Build tool | 3.8.0 or higher |
| **MySQL Database** | Relational database | 8.0 or higher |
| **RAM** | Minimum memory | 4GB (8GB recommended) |
| **Disk Space** | Storage requirement | 2GB minimum |

#### Verify Installations
```bash
# Verify Java
java -version

# Verify Maven
mvn -version

# Verify MySQL
mysql --version
```

### 10.2 MySQL Database Setup

#### Step 1: Start MySQL Service
```bash
# Windows
mysql -u root -p

# Linux/Mac
sudo systemctl start mysql
```

#### Step 2: Create Database and User

```sql
-- Login as root
mysql -u root -p

-- Create database
CREATE DATABASE peopleflow;

-- Create user
CREATE USER 'hrmsuser'@'localhost' IDENTIFIED BY 'hrmspass123';

-- Grant privileges
GRANT ALL PRIVILEGES ON peopleflow.* TO 'hrmsuser'@'localhost';
FLUSH PRIVILEGES;

-- Verify
SHOW DATABASES;
```

### 10.3 Application Configuration

#### Step 1: Update application.properties

Edit `src/main/resources/application.properties`:

```properties
# DATABASE CONFIGURATION
spring.datasource.url=jdbc:mysql://localhost:3306/peopleflow
spring.datasource.username=hrmsuser
spring.datasource.password=hrmspass123
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA/HIBERNATE CONFIGURATION
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.open-in-view=false

# SERVER CONFIGURATION
server.port=8443

# SSL/CERTIFICATE CONFIGURATION
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore/peopleflow.p12
server.ssl.key-store-password=peopleflow123
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=peopleflow

# JWT CONFIGURATION
app.jwt.secret=CHANGE_THIS_TO_A_LONG_SECURE_KEY_IN_PRODUCTION
app.jwt.expiration=36000000

# EMAIL CONFIGURATION
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# FILE UPLOAD CONFIGURATION
spring.servlet.multipart.max-file-size=20MB
spring.servlet.multipart.max-request-size=20MB
```

### 10.4 Maven Build Process

#### Step 1: Clean and Compile
```bash
cd C:\Users\sudhe\Downloads\hrapp\hrapp\

# Clean previous builds
mvn clean

# Compile source code
mvn compile
```

#### Step 2: Run Tests
```bash
mvn test
```

#### Step 3: Build Application JAR
```bash
mvn package
# Output: target/hrapp-0.0.1-SNAPSHOT.jar
```

### 10.5 Running the Application

#### Option 1: Run JAR File
```bash
cd C:\Users\sudhe\Downloads\hrapp\hrapp\
java -jar target/hrapp-0.0.1-SNAPSHOT.jar
```

#### Option 2: Run with Maven
```bash
mvn spring-boot:run
```

#### Option 3: Run from IDE
- Open project in Eclipse/IntelliJ
- Right-click on `HrappApplication.java`
- Select "Run As" → "Spring Boot App"

### 10.6 Verify Application Startup

#### Check Connection
```bash
# HTTPS endpoint
https://localhost:8443/

# API endpoint test
curl -k https://localhost:8443/api/auth/test

# Swagger UI (if enabled)
https://localhost:8443/swagger-ui.html
```

### 10.7 Initial User Setup

#### Database Seeding
```sql
-- Add initial admin user
INSERT INTO user (id, username, email, password, is_active)
VALUES (UUID(), 'admin', 'admin@peopleflow.com', 
        '$2a$10$hash_of_password', 1);
```

#### Default Login Credentials
| Username | Password | Role |
|----------|----------|------|
| admin | (configure) | ADMIN |
| hr_manager | (configure) | HR_MANAGER |

### 10.8 Post-Deployment Checklist

- [ ] Application starts without errors
- [ ] MySQL connection successful
- [ ] SSL certificate loaded
- [ ] Login functionality working
- [ ] Database tables created
- [ ] Email service configured
- [ ] File upload folder writable
- [ ] Scheduled tasks running
- [ ] Application logs monitored

---

## 11. API Endpoints

### 11.1 Authentication Endpoints

```
POST   /api/auth/register                    - Register new user
POST   /api/auth/login                       - User login
GET    /api/auth/validate-token              - Validate JWT token
POST   /api/auth/refresh-token               - Refresh token
POST   /api/auth/logout                      - Logout user
```

### 11.2 Employee Management Endpoints

```
GET    /api/employees                        - List all employees
GET    /api/employees/{id}                   - Get employee by ID
POST   /api/employees                        - Create new employee
PUT    /api/employees/{id}                   - Update employee
DELETE /api/employees/{id}                   - Delete employee
```

### 11.3 Attendance Endpoints

```
POST   /api/attendance/check-in              - Mark check-in
POST   /api/attendance/check-out             - Mark check-out
GET    /api/attendance/{employeeId}          - Get attendance history
GET    /api/attendance/report                - Attendance report
```

### 11.4 Timesheet Endpoints

```
POST   /api/timesheet/create                 - Create timesheet
GET    /api/timesheet/{id}                   - Get timesheet
PUT    /api/timesheet/{id}                   - Update timesheet
PUT    /api/timesheet/{id}/approve           - Approve timesheet
```

### 11.5 Leave Management Endpoints

```
POST   /api/leave/apply                      - Apply for leave
GET    /api/leave/{id}                       - Get leave request
PUT    /api/leave/{id}/approve               - Approve leave
PUT    /api/leave/{id}/reject                - Reject leave
GET    /api/leave/balance/{empId}            - Leave balance
```

### 11.6 Payroll Endpoints

```
GET    /api/payroll/employee/{empId}         - Employee salary
PUT    /api/payroll/update                   - Update salary
POST   /api/payroll/generate-payslip         - Generate payslip
GET    /api/payroll/report/monthly           - Monthly payroll report
```

### 11.7 Recruitment Endpoints

```
POST   /api/candidates/apply                 - Submit application
GET    /api/candidates                       - List candidates
GET    /api/candidates/{id}                  - Candidate details
PUT    /api/candidates/{id}/shortlist        - Shortlist candidate
PUT    /api/candidates/{id}/select           - Select candidate
POST   /api/candidates/{id}/send-offer       - Send offer letter
```

### 11.8 Travel Endpoints

```
POST   /api/travel/request                   - Submit travel request
GET    /api/travel/{id}                      - Get travel request
PUT    /api/travel/{id}/approve              - Approve travel
PUT    /api/travel/{id}/reject               - Reject travel
```

### 11.9 Admin Endpoints

```
GET    /api/audit-logs                       - Get audit logs
GET    /api/users                            - List users
PUT    /api/users/{id}                       - Update user
GET    /api/dashboard/metrics                - Dashboard metrics
```

---

## 12. Configuration Guide

### 12.1 Key Configuration Properties

#### Database Configuration
```properties
spring.datasource.url=jdbc:mysql://host:port/databasename
spring.datasource.username=username
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update
```

#### JWT Configuration
```properties
app.jwt.secret=your-long-secure-secret-key-min-32-chars
app.jwt.expiration=36000000
# Expiration times:
# 3600000 = 1 hour
# 86400000 = 24 hours
# 604800000 = 7 days
```

#### Email Configuration
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=app-password
```

#### SSL/HTTPS Configuration
```properties
server.port=8443
server.ssl.enabled=true
server.ssl.key-store=classpath:keystore/peopleflow.p12
server.ssl.key-store-password=store-password
server.ssl.key-store-type=PKCS12
```

### 12.2 Application Profiles

```bash
# Development
java -jar app.jar --spring.profiles.active=dev

# Production
java -jar app.jar --spring.profiles.active=prod

# Test
java -jar app.jar --spring.profiles.active=test
```

### 12.3 Server Configuration

```properties
server.port=8443
server.servlet.session.timeout=1800
server.tomcat.threads.max=200
```

---

## 13. Known Limitations

### 13.1 Current System Limitations

| Limitation | Impact | Workaround |
|-----------|--------|-----------|
| **Single-timezone Support** | Global offices can't have timezone conversions | Implement timezone conversion library |
| **Basic Audit Logging** | No real-time audit dashboard | Implement ELK stack for advanced logging |
| **No Microservices** | All features in monolithic app | Future: Migrate to microservices |
| **Limited Reporting** | Basic reports only | Add JasperReports or Crystal Reports |
| **No API Rate Limiting** | Vulnerable to API abuse | Add Spring Cloud Gateway or bucket4j |
| **Single Database Server** | Single point of failure | Implement database replication |
| **No Mobile App** | Only web interface available | Develop mobile application |

### 13.2 Performance Limitations

| Scenario | Current Limit | Notes |
|----------|---------------|-------|
| **Concurrent Users** | ~1000 users | Increase with load balancing |
| **File Upload Size** | 20MB per file | Configurable in properties |
| **Report Generation** | 100K+ records slow | Implement pagination/async |
| **Email Sending** | ~100 emails/minute | Limited by Gmail rate limits |

---

## 14. Troubleshooting Guide

### 14.1 Application Startup Issues

#### Issue: "Port 8443 already in use"
**Cause:** Another application using port 8443  
**Solution:**
```bash
# Find process using port
netstat -ano | findstr :8443    # Windows
lsof -i :8443                   # Linux/Mac

# Change port in application.properties
server.port=9443
```

#### Issue: "Could not locate driver - com.mysql.cj.jdbc.Driver"
**Cause:** MySQL JDBC driver not in classpath  
**Solution:**
- Verify `pom.xml` contains MySQL dependency
- Run: `mvn clean install`

#### Issue: "Connection refused - java.sql.SQLException"
**Cause:** MySQL server not running or wrong credentials  
**Solution:**
```bash
# Start MySQL
mysql -u root -p

# Verify connection
mysql -h localhost -u hrmsuser -p peopleflow
```

#### Issue: "JAVA_HOME is not set"
**Cause:** Java environment variable missing  
**Solution:**
```bash
# Windows
setx JAVA_HOME "C:\Program Files\Java\jdk-17"

# Verify
java -version
```

### 14.2 Database Issues

#### Issue: "Access denied for user 'hrmsuser'@'localhost'"
**Cause:** Wrong password or user doesn't exist  
**Solution:**
```sql
-- Reset password
ALTER USER 'hrmsuser'@'localhost' IDENTIFIED BY 'new_password';
FLUSH PRIVILEGES;
```

#### Issue: "Unknown database 'peopleflow'"
**Cause:** Database doesn't exist  
**Solution:**
```sql
CREATE DATABASE peopleflow;
```

#### Issue: "SQLException: Cannot get a connection, pool error"
**Cause:** Connection pool exhausted  
**Solution:**
```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
```

### 14.3 Email Issues

#### Issue: "535 5.7.8 Authentication failed"
**Cause:** Incorrect Gmail password  
**Solution:**
1. Generate App Password from Gmail
2. Update `application.properties` with correct app password

#### Issue: "javax.mail.SendFailedException: 550 User not found"
**Cause:** Invalid recipient email  
**Solution:**
- Validate email format
- Check recipient email exists

### 14.4 JWT/Security Issues

#### Issue: "JWT signature does not match"
**Cause:** Token signed with different secret key  
**Solution:**
- Ensure `app.jwt.secret` is identical on validation
- Don't change secret key mid-production

#### Issue: "Token has expired"
**Cause:** JWT token past expiration time  
**Solution:**
```properties
app.jwt.expiration=86400000  # 24 hours
```

#### Issue: "Certificate not found in keystore"
**Cause:** SSL certificate path incorrect  
**Solution:**
- Verify file exists in `src/main/resources/keystore/`

### 14.5 Performance Issues

#### Issue: "Slow application response"
**Cause:** Inefficient database queries  
**Solution:**
```properties
spring.jpa.show-sql=true
# Analyze queries and add indexes
```

#### Issue: "OutOfMemoryError: Java heap space"
**Cause:** Insufficient memory allocation  
**Solution:**
```bash
java -Xms2g -Xmx4g -jar app.jar
```

---

## 15. Future Enhancements

### 15.1 Immediate Enhancements (3-6 months)

| Feature | Benefit | Effort |
|---------|---------|--------|
| **Multi-factor Authentication (MFA)** | Enhanced security | Medium |
| **Mobile App (iOS/Android)** | On-the-go access | High |
| **Advanced Reporting** | Better insights | Medium |
| **Document Management System** | Centralized storage | Medium |

### 15.2 Advanced Features (6-12 months)

| Feature | Benefit | Effort |
|---------|---------|--------|
| **Microservices Architecture** | Scalability | High |
| **Machine Learning Models** | Predictive analytics | High |
| **Advanced Analytics Dashboard** | Real-time insights | Medium |
| **External System Integration** | Seamless data flow | Medium |

### 15.3 Enterprise Features (12+ months)

| Feature | Benefit | Effort |
|---------|---------|--------|
| **Multi-tenant Support** | SaaS capability | Very High |
| **Advanced Security** | Enterprise compliance | High |
| **AI/Automation** | Workflow automation | Very High |

### 15.4 Technology Upgrades

| Current | Future | Reason |
|---------|--------|--------|
| **Spring Boot 4.0.5** | Spring Boot 5.0+ | Latest features |
| **MySQL 8.0** | PostgreSQL 15+ | Better performance |
| **JWT** | OAuth 2.0/OpenID Connect | Industry standard |
| **Thymeleaf** | React/Angular | Modern UI |
| **Monolithic** | Microservices | Scalability |

### 15.5 Scalability Enhancements

```
Load Balancer (Nginx/HAProxy)
    ├─ App Instance 1 (Port 8443)
    ├─ App Instance 2 (Port 8444)
    └─ App Instance N (Port 844X)
            ↓
    Database Cluster (Master-Slave)
            ↓
    Cache Layer (Redis Cluster)
```

### 15.6 Estimated Timeline & Resources

| Initiative | Timeline | Team Size | Story Points |
|-----------|----------|-----------|--------------|
| **MFA** | 2-3 weeks | 2 | 13-21 |
| **Mobile App** | 3-4 months | 4-5 | 100-150 |
| **Microservices** | 6-9 months | 6-8 | 300-500 |
| **AI/ML** | 4-6 months | 3-4 | 150-200 |

---

## Appendix: Quick Reference

### A.1 Important Ports & URLs

| Service | Port | URL |
|---------|------|-----|
| **Application** | 8443 | https://localhost:8443 |
| **MySQL** | 3306 | localhost:3306 |
| **Swagger UI** | 8443 | https://localhost:8443/swagger-ui.html |

### A.2 Maven Commands Quick Reference

```bash
mvn clean                    # Clean build artifacts
mvn compile                  # Compile source code
mvn test                     # Run tests
mvn package                  # Build JAR
mvn spring-boot:run          # Run application
mvn dependency:tree          # Show dependency tree
```

### A.3 Database Commands

```bash
# Backup MySQL
mysqldump -u user -p database > backup.sql

# Restore MySQL
mysql -u user -p database < backup.sql

# Check table statistics
SELECT TABLE_NAME, TABLE_ROWS FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA='peopleflow';
```

---

## Document History

| Date | Version | Changes |
|------|---------|---------|
| 2026-06-25 | 1.0 | Initial handover document creation |

---

## Disclaimer

This document is provided as-is for handover purposes. The information contained herein is accurate as of the date of creation. The development team is responsible for maintaining this documentation as the system evolves.

**Last Update:** June 25, 2026  
**Document Owner:** Development Team  
**Access Level:** Internal—PeopleFlow HRMS Project

---

**END OF DOCUMENT**
