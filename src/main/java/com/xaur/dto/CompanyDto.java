package com.xaur.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDto {
    private Long id;
    @NotNull(message = "Name cannot be null")
    private String name;
    @NotNull(message = "shortCode can not be null")
    private String shortCode;

    private boolean active;
    @NotNull(message = "externalcode cannot be null")
    private String externalCode;
    @NotNull(message = "externalId cannot be null")
    private String externalId;
}
