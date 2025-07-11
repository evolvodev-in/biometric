package com.xaur.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_log_status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceLogStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_serial_number", nullable = false)
    private String deviceSerialNumber;

    @Column(name = "terminal_type")
    private String terminalType;

    @Column(name = "terminal_id")
    private String terminalId;

    @Column(name = "log_count")
    private Integer logCount;

    @Column(name = "max_count")
    private Integer maxCount;

    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}