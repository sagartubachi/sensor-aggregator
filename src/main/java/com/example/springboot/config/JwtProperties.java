package com.example.springboot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/*
This class holds the sensitive information about the JWT tokens that are generated
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {

    // Secret using which the JWT tokens are generated
    private String secret = "mySecretKey123456789012345678901234567890";

    // 100 days in seconds to make sure the generated JWT tokens work long enough after
    // the assignment is submitted
    private long expiration = 8640000;
    private String issuer = "iot-sensor-app";
    
    public String getSecret() {
        return secret;
    }
    
    public void setSecret(String secret) {
        this.secret = secret;
    }
    
    public long getExpiration() {
        return expiration;
    }
    
    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
