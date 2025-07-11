package com.xaur.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchResponse {
    private Long id;
    private String name;
    private CompanyResponse company;
    private boolean active;
    private String externalCode;
    private String externalId;
}
