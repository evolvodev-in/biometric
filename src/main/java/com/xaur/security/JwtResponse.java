package com.xaur.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    private String username;
    private String roles;
    private Long userId;


    public JwtResponse(String token) {
        this.token = token;
    }
}