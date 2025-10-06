package com.example.springboot.controller;

import com.example.springboot.security.JwtService;
import com.example.springboot.config.JwtProperties;
import com.example.springboot.bean.LoginRequest;
import com.example.springboot.bean.LoginResponse;
import com.example.springboot.service.IngestService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/*
This class generates the JWT token on calling the /api/auth API if the user name and password is correctly provided
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtProperties jwtProperties;

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {

        log.info("AuthController : login attempted for user name {}", loginRequest.getUsername());

        // Authenticate the user name and password
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        log.info("AuthController : Generating token for user name {}", loginRequest.getUsername());

        // Generate the JWT token
        String token = jwtService.generateToken(loginRequest.getUsername());

        log.info("AuthController : Generated token for user name {}. The token will expire in {}", loginRequest.getUsername(), jwtProperties.getExpiration());

        // Respond with the JWT token along with expiry information
        return ResponseEntity.ok(new LoginResponse(token, jwtProperties.getExpiration()));
    }
}
