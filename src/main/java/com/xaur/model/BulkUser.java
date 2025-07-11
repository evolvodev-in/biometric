package com.xaur.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Table(name = "user")
@Data
public class BulkUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
