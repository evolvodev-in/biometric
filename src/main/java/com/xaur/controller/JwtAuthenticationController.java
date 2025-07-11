package com.xaur.controller;

import com.xaur.model.AdminUser;
import com.xaur.repository.AdminUserRepository;
import com.xaur.security.AdminUserDetailsService;
import com.xaur.security.JwtRequest;
import com.xaur.security.JwtResponse;
import com.xaur.security.JwtTokenUtil;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class JwtAuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final AdminUserDetailsService userDetailsService;
    private final AdminUserRepository adminUserRepository;

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
        try{
            Authentication authentication=authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(),authenticationRequest.getPassword()));
            if(!authentication.isAuthenticated())
                throw new Exception("Invalid Credentials");

        }
        catch (Exception e){
            Map<String,String> map=new HashMap<String,String>();
            map.put("message","Invalid Username/Password");

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(map);
        }

        AdminUser adminUser = adminUserRepository.findByUsername(authenticationRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + authenticationRequest.getUsername()));

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String token = jwtTokenUtil.generateToken(userDetails);

        // Create response with user details
        JwtResponse response = JwtResponse.builder()
                .token(token)
                .username(adminUser.getUsername())
                .roles(adminUser.getRoles())
                .userId(adminUser.getId())
                .build();
        Map<String,Object> map=new HashMap<>();
        map.put("data",response);
        map.put("message","success");

        return ResponseEntity.ok(map);
    }

    /*private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (Exception e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }*/
}