package com.xaur.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "device_status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class DeviceStatus {

    @Id
   @Column(name = "device_serial_no", nullable = false)
    private String deviceSerialNo;
@Column(name = "terminal_type")
    private String terminalType;
@Column(name = "terminal_id")
    private String terminalId;
@Column(name ="product_name")
    private String productName;
@Column(name = "device_uid" )
    private String deviceUid;

   @Column(name = "manager_count")
    private Integer managerCount;
   @Column(name = "user_count")
    private Integer userCount;
   @Column(name = "face_count")
    private Integer faceCount;
   @Column(name = "fp_count")
    private Integer fpCount;
   @Column(name = "card_count")
    private Integer cardCount;
   @Column(name = "pwd_count")
    private Integer pwdCount;
   @Column(name = "door_status")
    private Integer doorStatus;
   @Column(name = "alarm_status")
    private Integer alarmStatus;
@Column(name = "firmware_version")
    private String firmwareVersion;
@Column(name = "build_number")
    private String buildNumber;

    @Column(name = "device_time")
    private LocalDateTime deviceTime;
    @Column(name = "last_online")
    private LocalDateTime lastOnline;

    @Column(name = "last_status_update")
    private LocalDateTime lastStatusUpdate;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "company_id")
    private Company company;

    private boolean online;
    @PrePersist
    @PreUpdate
    public void validateDeviceSerialNo() {
        if (deviceSerialNo == null || deviceSerialNo.trim().isEmpty()) {
            throw new IllegalStateException("Device serial number cannot be null or empty");
        }
    }

}