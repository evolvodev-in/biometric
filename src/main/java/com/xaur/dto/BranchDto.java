package com.xaur.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BranchDto {
    private Long id;
    @NotNull(message = "Name cannot be null")
    private String name;
    private Long companyId;
    private boolean active;
    private String externalCode;
    private String externalId;
}