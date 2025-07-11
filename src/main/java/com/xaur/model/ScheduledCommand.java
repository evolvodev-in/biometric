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
@Table(name = "command_queue")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_serial_number", nullable = false)
    private String deviceSerialNumber;

    @Column(name = "command_type", nullable = false)
    private String commandType;

    @Column(name = "command_xml", columnDefinition = "TEXT", nullable = false)
    private String commandXml;

    @Column(name = "status", nullable = false)
    private String status; 

    @Column(name = "user_id")
    private String userId;

    @Column(name = "response_xml", columnDefinition = "TEXT")
    private String responseXml;

    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}