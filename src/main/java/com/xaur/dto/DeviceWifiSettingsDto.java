package com.xaur.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceWifiSettingsDto {
    private String deviceSerialNumber;
    private String terminalType;
    private String terminalId;
    private String use;
    private String ssid;
    private String key;
    private String dhcp;
    private String ip;
    private String subnet;
    private String defaultGateway;
    private Integer port;
    private String ipFromDhcp;
    private String subnetFromDhcp;
    private String defaultGatewayFromDhcp;
    private String result;
    private String lastSyncTime;
}