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
@Table(name = "device_additional_info")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceAdditionalInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_serial_number", nullable = false)
    private String deviceSerialNumber;

    @Column(name = "terminal_type")
    private String terminalType;

    @Column(name = "terminal_id")
    private String terminalId;

    @Column(name = "param_name", nullable = false)
    private String paramName;

    @Column(name = "value1", columnDefinition = "TEXT")
    private String value1;

    @Column(name = "value2", columnDefinition = "TEXT")
    private String value2;

    @Column(name = "value3", columnDefinition = "TEXT")
    private String value3;

    @Column(name = "value4", columnDefinition = "TEXT")
    private String value4;

    @Column(name = "value5", columnDefinition = "TEXT")
    private String value5;

    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}