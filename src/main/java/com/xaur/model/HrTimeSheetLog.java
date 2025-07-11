package com.xaur.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Data
@Getter
@Setter
@Table(name = "hr_timesheet_log")

public class HrTimeSheetLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "date_code")

    private LocalDate dateCode;
    @Column(name = "employee_ref_code")
    private String employeeRefCode;
    @Column(name = "employee_name")
    private String employeeName;
    @Column(name = "application")
    private String application;
    @Column(name = "att_status")
    private String attStatus;
    @Column(name = "att_date")
    private LocalDate attDate;
    @Column(name = "att_time")
    private LocalTime attTime;
    @Column(name = "log_date")
    private LocalDateTime logDate;
    @Column(name = "location")
    private String location;
    @Column(name = "device_id")
    private String deviceId;
    @Column(name = "device_log_id")
    private Long deviceLogId;
    @Column(name = "shift")
    private String shift;
    @Column(name = "status")
    private String status;
    @Column(name = "modify_status")
    private String modifyStatus;
    @Column(name = "image_file")
    private String imageFile;
    @Column(name = "division")
    private String division;
    @Column(name = "is_modified")
    private Boolean isModified;
    @Column(name = "is_processed")
    private Boolean isProcessed;
    @Column(name = "timesheet_id")
    private Long timeSheetId;

    @Column(name = "is_transfered")
    private Boolean isTransfered;

    @Column(name = "comments")
    private String comments;
    @Column(name = "branch_code")
    private String branchCode;
    @Column(name = "company_code")
    private String companyCode;
    @Column(name = "createdby")
    private String createdBy;
    @Column(name = "createddate")
    private LocalDateTime createdDate;


}

