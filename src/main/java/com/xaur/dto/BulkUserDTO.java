package com.xaur.dto;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
@Data
public class BulkUserDTO {


    private Long id;
    private String user_id;
    private String device_serial_number;

    private String name;
    private String privilege;
    private boolean enabled;

    private Boolean user_period_used;


    private Integer department
            ;

    private String status;
}
