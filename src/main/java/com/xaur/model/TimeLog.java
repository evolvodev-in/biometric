package com.xaur.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "time_logs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_id")
    private String logId;

    @Column(name = "device_serial_number")
    private String deviceSerialNumber;

    @Column(name = "user_id")
    private String userId;
@Transient
private String userName;

    @Column(name = "log_time")
    private LocalDateTime logTime;

    @Column(name = "action")
    private String action;

    @Column(name = "attend_stat")
    private String attendStat;

    @Column(name = "ap_stat")
    private String apStat;

    @Column(name = "job_code")
    private Integer jobCode;

    @Column(name = "has_photo")
    private boolean hasPhoto;

    @Column(name = "log_image", columnDefinition = "LONGTEXT")
    private String logImage;

    @Column(name = "trans_id")
    private String transId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}