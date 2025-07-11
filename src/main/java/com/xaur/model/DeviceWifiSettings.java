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
@Table(name = "device_wifi_settings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceWifiSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "device_serial_number", nullable = false, unique = true)
    private String deviceSerialNumber;

    @Column(name = "terminal_type")
    private String terminalType;

    @Column(name = "terminal_id")
    private String terminalId;

    @Column(name = "wifi_use")
    private String use;

    @Column(name = "ssid")
    private String ssid;

    @Column(name = "wifi_key", columnDefinition = "TEXT")
    private String key;

    @Column(name = "dhcp")
    private String dhcp;

    @Column(name = "ip")
    private String ip;

    @Column(name = "subnet")
    private String subnet;

    @Column(name = "default_gateway")
    private String defaultGateway;

    @Column(name = "port")
    private Integer port;

    @Column(name = "ip_from_dhcp")
    private String ipFromDhcp;

    @Column(name = "subnet_from_dhcp")
    private String subnetFromDhcp;

    @Column(name = "default_gateway_from_dhcp")
    private String defaultGatewayFromDhcp;

    @Column(name = "result")
    private String result;

    @Column(name = "last_sync_time")
    private LocalDateTime lastSyncTime;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}