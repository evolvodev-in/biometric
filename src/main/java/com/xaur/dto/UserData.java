package com.xaur.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserData {
    private String name;
    private String privilege;
    private Boolean enabled;
    private Integer timeSet1;
    private Integer timeSet2;
    private Integer timeSet3;
    private Integer timeSet4;
    private Integer timeSet5;
    private Boolean userPeriodUsed;
    private Integer userPeriodStart;
    private Integer userPeriodEnd;
    private String card;
    private String password;
    private String faceData;

    private Boolean allowNoCertificate;
    private Integer Department;
}