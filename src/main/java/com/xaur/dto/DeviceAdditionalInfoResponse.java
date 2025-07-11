package com.xaur.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceAdditionalInfoResponse {
    private String status;
    private String message;
    private DeviceAdditionalInfoDto data;
}