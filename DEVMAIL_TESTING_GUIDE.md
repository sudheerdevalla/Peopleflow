# DevMailTestController - Development Email Testing Guide

## Overview
The `DevMailTestController` is a development-only REST controller that allows you to test all email workflows without waiting for scheduled cron jobs. It is **automatically disabled in production** via Spring profile configuration.

---

## 🔐 Security & Profile Management

### Profile Configuration
- **Development Profile (`dev`)**: All test endpoints ENABLED
- **Production Profile (`prod`)**: All test endpoints DISABLED
- **Default (no profile)**: Test endpoints DISABLED (safe default)

### How Profiles Work
- The controller uses `@Profile("dev")` annotation - it only loads when the `dev` Spring profile is active
- Profile-specific property files:
  - `application-dev.properties` - Development settings
  - `application-prod.properties` - Production settings (safe/secure)

### Enable Dev Profile

#### Option 1: Environment Variable
```bash
export SPRING_PROFILES_ACTIVE=dev
```

#### Option 2: application.properties
Add this line to `src/main/resources/application.properties`:
```ini
spring.profiles.active=dev
```

#### Option 3: application-dev.properties (Recommended - auto-picked if you set profile)
Already created and contains all necessary dev config.

#### Option 4: JVM Argument
```bash
java -Dspring.profiles.active=dev -jar hrapp-0.0.1-SNAPSHOT.jar
```

#### Option 5: Maven Plugin
```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=dev
```

### Verification
After starting with `dev` profile, check logs for:
```
The following profiles are active: dev
```

If you see this, the dev controller is enabled and accessible.

---

## 📋 Available Test Endpoints

All endpoints are accessible at `https://localhost:8443/dev/*`

### 1. Test Welcome Mail
**Endpoint:** `GET /dev/test-welcome-mail`
- **Purpose**: Send a welcome email to a new employee
- **Parameters**: `email` (optional, default: `sudheerdevalla950214@gmail.com`)
- **Example URL**: 
  ```
  https://localhost:8443/dev/test-welcome-mail?email=test@example.com
  ```
- **Response**:
  ```json
  {
    "status": "SUCCESS",
    "endpoint": "test-welcome-mail",
    "message": "Welcome email sent to: test@example.com"
  }
  ```

### 2. Test Forgot Password
**Endpoint:** `GET /dev/test-forgot-password`
- **Purpose**: Send OTP for password reset
- **Parameters**: `email` (optional, default: `sudheerdevalla950214@gmail.com`)
- **Example URL**:
  ```
  https://localhost:8443/dev/test-forgot-password?email=user@example.com
  ```
- **Response**:
  ```json
  {
    "status": "SUCCESS",
    "endpoint": "test-forgot-password",
    "message": "Forgot password email sent with OTP: 123456"
  }
  ```

### 3. Test Payslip
**Endpoint:** `GET /dev/test-payslip`
- **Purpose**: Generate and send password-protected payslip PDF via email
- **Parameters**: `employeeId` (optional, uses first employee if not specified)
- **Example URL**:
  ```
  https://localhost:8443/dev/test-payslip?employeeId=1
  ```
- **Response**:
  ```json
  {
    "status": "SUCCESS",
    "endpoint": "test-payslip",
    "message": "Payslip generated and sent to: employee@example.com"
  }
  ```
- **Notes**:
  - Generates a password-protected PDF with format: `FirstTwo + LastTwoDOBYearDigits`
  - Requires at least one employee in database
  - Payroll must be calculable for the employee

### 4. Test CEO Report
**Endpoint:** `GET /dev/test-ceo-report`
- **Purpose**: Generate consolidated payroll report and send to CEO/Admin email
- **Parameters**: None
- **Example URL**:
  ```
  https://localhost:8443/dev/test-ceo-report
  ```
- **Response**:
  ```json
  {
    "status": "SUCCESS",
    "endpoint": "test-ceo-report",
    "message": "CEO consolidated payroll report sent"
  }
  ```
- **Notes**:
  - Uses `app.admin.email` from `application-dev.properties`
  - Generates an Excel file with payroll data

### 5. Test Leave Approval
**Endpoint:** `GET /dev/test-leave-approval`
- **Purpose**: Send leave approval email to employee
- **Parameters**: None
- **Example URL**:
  ```
  https://localhost:8443/dev/test-leave-approval
  ```
- **Response**:
  ```json
  {
    "status": "SUCCESS",
    "endpoint": "test-leave-approval",
    "message": "Leave approval email sent to: employee@example.com"
  }
  ```
- **Notes**:
  - Requires at least one PENDING leave in database
  - If no pending leave, returns FAILED status

### 6. Test Leave Rejection
**Endpoint:** `GET /dev/test-leave-rejection`
- **Purpose**: Send leave rejection email to employee
- **Parameters**: None
- **Example URL**:
  ```
  https://localhost:8443/dev/test-leave-rejection
  ```
- **Response**:
  ```json
  {
    "status": "SUCCESS",
    "endpoint": "test-leave-rejection",
    "message": "Leave rejection email sent to: employee@example.com"
  }
  ```

### 7. Test Timesheet Reminder
**Endpoint:** `GET /dev/test-timesheet-reminder`
- **Purpose**: Execute timesheet reminder scheduler immediately
- **Parameters**: None
- **Example URL**:
  ```
  https://localhost:8443/dev/test-timesheet-reminder
  ```
- **Response**:
  ```json
  {
    "status": "SUCCESS",
    "endpoint": "test-timesheet-reminder",
    "message": "Timesheet reminder scheduler executed"
  }
  ```
- **Notes**:
  - Sends reminders for missing day-1, day-3, day-5 timesheets
  - Creates TimesheetPenalty records
  - May deduct leaves after day 5

### 8. Test Payroll Scheduler
**Endpoint:** `GET /dev/test-payroll-scheduler`
- **Purpose**: Execute payroll scheduler immediately (generates & sends payslips to all employees)
- **Parameters**: None
- **Example URL**:
  ```
  https://localhost:8443/dev/test-payroll-scheduler
  ```
- **Response**:
  ```json
  {
    "status": "SUCCESS",
    "endpoint": "test-payroll-scheduler",
    "message": "Payroll scheduler executed (payslips sent to all employees)"
  }
  ```
- **⚠️ WARNING**: This sends emails to ALL employees in the database

### 9. Test Travel Mail
**Endpoint:** `GET /dev/test-travel-mail`
- **Purpose**: Send travel request notification email to manager
- **Parameters**: None
- **Example URL**:
  ```
  https://localhost:8443/dev/test-travel-mail
  ```
- **Response**:
  ```json
  {
    "status": "SUCCESS",
    "endpoint": "test-travel-mail",
    "message": "Travel notification email sent to manager: manager@example.com"
  }
  ```
- **Notes**:
  - Requires at least one REQUESTED travel request in database
  - Requires traveler to have a manager assigned

### 10. Test All Mails (Comprehensive Test)
**Endpoint:** `GET /dev/test-all-mails`
- **Purpose**: Execute every email workflow in sequence with summary report
- **Parameters**: None
- **Example URL**:
  ```
  https://localhost:8443/dev/test-all-mails
  ```
- **Response**:
  ```json
  {
    "summary": {
      "total": 9,
      "passed": 8,
      "failed": 1
    },
    "results": [
      {
        "endpoint": "test-welcome-mail",
        "status": "✔ Success",
        "message": ""
      },
      {
        "endpoint": "test-forgot-password",
        "status": "✔ Success",
        "message": ""
      },
      {
        "endpoint": "test-payslip",
        "status": "✔ Success",
        "message": ""
      },
      {
        "endpoint": "test-ceo-report",
        "status": "✔ Success",
        "message": ""
      },
      {
        "endpoint": "test-leave-approval",
        "status": "✖ Failed",
        "message": "No pending leaves found"
      },
      ...
    ]
  }
  ```
- **Features**:
  - Continues even if one test fails
  - Returns pass/fail for each endpoint
  - Includes failure reasons
  - Summary at the end
  - Full logs printed to console/logs

---

## 🧪 How to Test Using Postman

### Import Collection
1. Open Postman
2. Create a new Collection called "HR App Email Tests"
3. Create requests for each endpoint (see below)

### Sample Postman Requests

#### Request 1: Welcome Mail
```
Method: GET
URL: https://localhost:8443/dev/test-welcome-mail
Headers: 
  - Content-Type: application/json
  - (If SSL cert not trusted: Disable SSL verification under Settings)
Params:
  - email: sudheerdevalla950214@gmail.com
```

#### Request 2: Test All Mails (Comprehensive)
```
Method: GET
URL: https://localhost:8443/dev/test-all-mails
Headers:
  - Content-Type: application/json
  - (If SSL cert not trusted: Disable SSL verification)
Params: (none)
```

---

## 🌐 How to Test Using Browser

1. Start the app with dev profile (e.g., `mvnw.cmd spring-boot:run`)
2. Navigate to: `https://localhost:8443/dev/test-welcome-mail`
3. You'll see the JSON response in the browser

**Note**: Browser doesn't support query parameters easily for complex scenarios. Use Postman for better UX.

---

## 📜 Using cURL (Command Line)

### Test Welcome Mail
```bash
curl -X GET "https://localhost:8443/dev/test-welcome-mail?email=test@example.com" \
  -H "Content-Type: application/json" \
  -k  # (ignore SSL cert warnings for self-signed cert)
```

### Test Payslip
```bash
curl -X GET "https://localhost:8443/dev/test-payslip?employeeId=1" \
  -H "Content-Type: application/json" \
  -k
```

### Test All Mails
```bash
curl -X GET "https://localhost:8443/dev/test-all-mails" \
  -H "Content-Type: application/json" \
  -k
```

---

## 📊 Test Results & Logs

### Console Logs
When you run a test, check the console for:
```
Testing Welcome Mail...
✓ Welcome Mail: Welcome email sent to: test@example.com

Testing Forgot Password...
✓ Forgot Password: Forgot password email sent with OTP: 123456
```

### Log File
Logs are also written to the application log file (usually `logs/spring.log` or similar).

### Success Response
```json
{
  "status": "SUCCESS",
  "endpoint": "test-welcome-mail",
  "message": "Welcome email sent to: test@example.com"
}
```

### Failure Response
```json
{
  "status": "FAILED",
  "endpoint": "test-payslip",
  "message": "Error: Employee not found: 999"
}
```

---

## ✅ Verification Checklist

After deploying to Oracle Cloud:

- [ ] Go to production profile: `spring.profiles.active=prod`
- [ ] Run the app on production profile
- [ ] Try to access: `https://your-prod-domain/dev/test-welcome-mail`
- [ ] Expected: **404 Not Found** or **403 Forbidden** (controller not loaded)
- [ ] Verify in logs: "The following profiles are active: prod" (No "dev" mentioned)
- [ ] If you see a 404/403, controller is **successfully disabled** ✓

**DO NOT** see:
- Any dev endpoint response
- Any "dev" profile in logs
- Access to `/dev/*` endpoints

---

## 🔒 Production Safety Guarantees

1. **Profile-Based Protection**
   - `@Profile("dev")` ensures controller only loads when "dev" profile is active
   - Default behavior (no profile specified) disables the controller

2. **Property Files**
   - `application-prod.properties` has dev features DISABLED
   - `app.features.dev-mail-tester-enabled=false` in production properties

3. **Code Review**
   - Controller only uses existing services (no new business logic)
   - All endpoints are read-only or test-only (no permanent data changes)
   - No sensitive data exposed in responses

4. **Deployment Process**
   - Use `spring.profiles.active=prod` environment variable on Oracle Cloud
   - Verify profile is set before deployment
   - Check logs after deployment to confirm "prod" profile is active

---

## 📝 Files Modified/Created

### New Files Created:
1. `src/main/java/com/hr/hrapp/controller/DevMailTestController.java` - Main test controller
2. `src/main/resources/application-dev.properties` - Development profile config
3. `src/main/resources/application-prod.properties` - Production profile config

### Files Already Updated (from previous changes):
1. `src/main/java/com/hr/hrapp/service/EmailService.java` - Uses unified From address
2. `src/main/java/com/hr/hrapp/controller/AuthController.java` - Uses unified From address
3. `src/main/java/com/hr/hrapp/payroll/report/CEOReportService.java` - Uses unified From address & configurable email
4. `src/main/resources/application.properties` - Base config (profiles override this)

---

## 🚀 Quick Start Commands

### Start with Dev Profile (Enable dev endpoints)
```bash
mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=dev
```

### Start without Dev Profile (Production-like)
```bash
mvnw.cmd spring-boot:run
```

### Test Welcome Mail
```bash
curl -X GET "https://localhost:8443/dev/test-welcome-mail?email=yourmail@example.com" -k
```

### Test Everything
```bash
curl -X GET "https://localhost:8443/dev/test-all-mails" -k | jq .
```

---

## 🐛 Troubleshooting

### Issue: "404 Not Found" when accessing /dev/* endpoints
**Solution**: Ensure `dev` profile is active
```bash
# Check current profile in logs - should show "active: dev"
# If not, start app with: -Dspring.profiles.active=dev
```

### Issue: "No employees found in database"
**Solution**: Create at least one employee in the database before testing payslip
```sql
INSERT INTO employees (name, email, basic_salary, status, role) 
VALUES ('Test Employee', 'test@example.com', 50000, 'Active', 'USER');
```

### Issue: SSL Certificate Error
**Solution**: Use `-k` flag with curl to ignore self-signed certificate
```bash
curl -k https://localhost:8443/dev/test-welcome-mail
```

### Issue: Mail is not being sent
**Solution**: Check Outlook 365 SMTP settings
1. Verify mail credentials in `application-dev.properties`
2. Check if SMTP Auth is enabled in Office 365
3. Check app password if using MFA
4. Look at error logs for detailed mail errors

---

## ✨ Features & Best Practices

✅ **What This Controller Does Well:**
- Isolates dev functionality from production
- Uses existing services (no code duplication)
- Comprehensive logging and error handling
- Safe to run multiple times
- Reusable for CI/CD test pipelines
- No permanent data modifications

✅ **What Won't Happen in Production:**
- Dev endpoints won't be compiled/loaded
- No `/dev/*` paths accessible
- No risk of accidental test email sending to customers
- No performance impact (controller not initialized)

---

## 📞 Support & Questions

For questions or issues:
1. Check controller source code: `DevMailTestController.java`
2. Review logs with DEBUG level enabled in `application-dev.properties`
3. Verify database has test data (employees, leaves, travel requests)
4. Ensure Office 365 SMTP credentials are correct

