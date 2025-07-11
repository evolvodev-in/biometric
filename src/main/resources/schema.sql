-- Create database
CREATE DATABASE IF NOT EXISTS attendance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create user and grant privileges
CREATE USER IF NOT EXISTS 'attendance_user'@'localhost' IDENTIFIED BY 'attendance_password';
GRANT ALL PRIVILEGES ON attendance_db.* TO 'attendance_user'@'localhost';
FLUSH PRIVILEGES;

-- Use the database
USE attendance_db;

-- Create devices table
CREATE TABLE IF NOT EXISTS devices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    serial_number VARCHAR(50) NOT NULL UNIQUE,
    terminal_type VARCHAR(50),
    terminal_id VARCHAR(50),
    token VARCHAR(100),
    cloud_id VARCHAR(100),
    is_registered BOOLEAN DEFAULT FALSE,
    is_logged_in BOOLEAN DEFAULT FALSE,
    last_connection_time DATETIME,
    last_activity_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_serial_number (serial_number),
    INDEX idx_token (token)
);

-- Create users table
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    name VARCHAR(255),
    privilege VARCHAR(50),
    department INT,
    enabled BOOLEAN DEFAULT TRUE,
    time_set_1 INT,
    time_set_2 INT,
    time_set_3 INT,
    time_set_4 INT,
    time_set_5 INT,
    user_period_used BOOLEAN DEFAULT FALSE,
    user_period_start INT,
    user_period_end INT,
    card TEXT,
    password VARCHAR(100),
    fingers TEXT,
    face_enrolled BOOLEAN DEFAULT FALSE,
    face_data LONGTEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id)
);

-- Create time_logs table
CREATE TABLE IF NOT EXISTS time_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    log_id VARCHAR(50),
    device_serial_number VARCHAR(50),
    user_id VARCHAR(50),
    log_time DATETIME,
    action VARCHAR(50),
    attend_stat VARCHAR(50),
    ap_stat VARCHAR(50),
    job_code INT,
    has_photo BOOLEAN DEFAULT FALSE,
    log_image LONGTEXT,
    trans_id VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_device_serial_number (device_serial_number),
    INDEX idx_user_id (user_id),
    INDEX idx_log_time (log_time)
);

-- Create admin_logs table
CREATE TABLE IF NOT EXISTS admin_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    log_id VARCHAR(50),
    device_serial_number VARCHAR(50),
    admin_id VARCHAR(50),
    user_id VARCHAR(50),
    log_time DATETIME,
    action VARCHAR(50),
    stat INT,
    trans_id VARCHAR(100),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_device_serial_number (device_serial_number),
    INDEX idx_admin_id (admin_id),
    INDEX idx_user_id (user_id),
    INDEX idx_log_time (log_time)
);