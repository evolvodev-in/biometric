package com.xaur.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "companies")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true,name="short_code")
    private String shortCode;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "external_code")
    private String externalCode;

    @Column(name = "external_id")
    private String externalId;
}