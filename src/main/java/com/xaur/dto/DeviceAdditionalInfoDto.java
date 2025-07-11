package com.xaur.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceAdditionalInfoDto {
    private String deviceSerialNumber;
    private String terminalType;
    private String terminalId;
    private String paramName;
    private String value1;
    private String value2;
    private String value3;
    private String value4;
    private String value5;
    private String lastSyncTime;
}