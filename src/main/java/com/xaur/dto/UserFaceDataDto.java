package com.xaur.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFaceDataDto {
    private String userId;
    private String faceEnrolled;
    private String faceData;
    private String deviceSerialNumber;
}