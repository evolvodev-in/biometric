package com.xaur.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceLogStatusDto {
    private String deviceSerialNumber;
    private String terminalType;
    private String terminalId;
    private Integer logCount;
    private Integer maxCount;
    private String lastSyncTime;
}