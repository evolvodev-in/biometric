package com.xaur.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceEthernetSettingsDto {
    private String deviceSerialNumber;
    private String terminalType;
    private String terminalId;
    private String dhcp;
    private String ip;
    private String subnet;
    private String defaultGateway;
    private Integer port;
    private String macAddress;
    private String ipFromDhcp;
    private String subnetFromDhcp;
    private String defaultGatewayFromDhcp;
    private String lastSyncTime;
}