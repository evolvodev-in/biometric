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
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "device_serial_number")
    private String deviceSerialNumber;

    @Column(name = "terminal_id")
    private String terminalId;

    @Column(name = "terminal_type")
    private String terminalType;

    @Column(name = "name")
    private String name;

    @Column(name = "privilege")
    private String privilege;

    @Column(name = "department")
    private Integer department;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "time_set_1")
    private Integer timeSet1;

    @Column(name = "time_set_2")
    private Integer timeSet2;

    @Column(name = "time_set_3")
    private Integer timeSet3;

    @Column(name = "time_set_4")
    private Integer timeSet4;

    @Column(name = "time_set_5")
    private Integer timeSet5;

    @Column(name = "user_period_used")
    private boolean userPeriodUsed;

    @Column(name = "user_period_start")
    private Integer userPeriodStart;

    @Column(name = "user_period_end")
    private Integer userPeriodEnd;

    @Column(name = "card", columnDefinition = "TEXT")
    private String card;

    @Column(name = "password")
    private String password;

    @Column(name = "fingers", columnDefinition = "TEXT")
    private String fingers;

    @Column(name = "face_enrolled")
    private boolean faceEnrolled;

    @Column(name = "face_data", columnDefinition = "LONGTEXT")
    private String faceData;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


}