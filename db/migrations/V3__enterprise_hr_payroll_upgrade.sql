-- Enterprise HR/Payroll upgrade
-- Additive and backward-compatible schema changes only.

ALTER TABLE timesheet ADD COLUMN IF NOT EXISTS reviewed_at DATETIME NULL;
ALTER TABLE timesheet ADD COLUMN IF NOT EXISTS reviewed_by VARCHAR(255) NULL;

ALTER TABLE travel_request ADD COLUMN IF NOT EXISTS payroll_reference_month VARCHAR(255) NULL;
ALTER TABLE travel_request ADD COLUMN IF NOT EXISTS payroll_processed_at DATETIME NULL;

ALTER TABLE payroll ADD COLUMN IF NOT EXISTS approved_additions DOUBLE NULL DEFAULT 0;
ALTER TABLE payroll ADD COLUMN IF NOT EXISTS gross_salary DOUBLE NULL DEFAULT 0;
ALTER TABLE payroll ADD COLUMN IF NOT EXISTS payable_days INT NULL DEFAULT 0;
ALTER TABLE payroll ADD COLUMN IF NOT EXISTS working_days INT NULL DEFAULT 0;
ALTER TABLE payroll ADD COLUMN IF NOT EXISTS status VARCHAR(50) NULL DEFAULT 'DRAFT';
ALTER TABLE payroll ADD COLUMN IF NOT EXISTS reconciliation_status VARCHAR(50) NULL DEFAULT 'PENDING';
ALTER TABLE payroll ADD COLUMN IF NOT EXISTS last_calculated_at DATETIME NULL;
ALTER TABLE payroll ADD COLUMN IF NOT EXISTS finalized_at DATETIME NULL;
ALTER TABLE payroll ADD COLUMN IF NOT EXISTS finalized_by VARCHAR(255) NULL;

ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS entity_type VARCHAR(255) NULL;
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS entity_id VARCHAR(255) NULL;
ALTER TABLE audit_log ADD COLUMN IF NOT EXISTS details VARCHAR(2000) NULL;

ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS original_file_name VARCHAR(255) NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS content_type VARCHAR(255) NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS storage_path VARCHAR(500) NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS file_hash VARCHAR(255) NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS extraction_status VARCHAR(100) NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS validation_status VARCHAR(100) NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS conflict_status VARCHAR(100) NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS review_status VARCHAR(100) NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS extracted_name VARCHAR(255) NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS extracted_email VARCHAR(255) NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS extracted_employee_code VARCHAR(255) NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS extracted_pan_number VARCHAR(255) NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS extracted_aadhaar_number VARCHAR(255) NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS extracted_pf_number VARCHAR(255) NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS extracted_uan_number VARCHAR(255) NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS extracted_account_number VARCHAR(255) NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS extracted_ifsc VARCHAR(255) NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS review_notes VARCHAR(2000) NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS reviewed_by VARCHAR(255) NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS reviewed_at DATETIME NULL;
ALTER TABLE employee_document ADD COLUMN IF NOT EXISTS uploaded_at DATETIME NULL;
CREATE UNIQUE INDEX IF NOT EXISTS uk_employee_document_file_hash ON employee_document(file_hash);

CREATE TABLE IF NOT EXISTS financial_access_otp (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    otp_hash VARCHAR(200) NOT NULL,
    expires_at DATETIME NULL,
    resend_allowed_at DATETIME NULL,
    failed_attempts INT NOT NULL DEFAULT 0,
    resend_count INT NOT NULL DEFAULT 0,
    verified BIT NOT NULL DEFAULT 0,
    active BIT NOT NULL DEFAULT 1,
    verified_at DATETIME NULL,
    created_at DATETIME NULL
);

CREATE INDEX IF NOT EXISTS idx_financial_access_otp_username_active
ON financial_access_otp(username, active, created_at);

CREATE INDEX IF NOT EXISTS idx_payroll_month_status ON payroll(month, status);
CREATE INDEX IF NOT EXISTS idx_payroll_employee_month ON payroll(employee_id, month);
CREATE INDEX IF NOT EXISTS idx_travel_payroll_processed ON travel_request(emp_id, payroll_processed, status);
