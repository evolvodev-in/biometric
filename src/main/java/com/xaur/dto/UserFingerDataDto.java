package com.xaur.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFingerDataDto {
    private String userId;
    private Integer fingerNo;
    private String duress;
    private String fingerData;
    private String deviceSerialNumber;
}