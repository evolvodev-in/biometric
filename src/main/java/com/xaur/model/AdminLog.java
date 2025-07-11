package com.xaur.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "admin_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_id")
    private String logId;

    @Column(name = "device_serial_number")
    private String deviceSerialNumber;

    @Column(name = "admin_id")
    private String adminId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "log_time")
    private LocalDateTime logTime;

    @Column(name = "action")
    private String action;

    @Column(name = "stat")
    private Integer stat;

    @Column(name = "trans_id")
    private String transId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}