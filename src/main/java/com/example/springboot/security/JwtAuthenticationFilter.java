package com.example.springboot.security;

import com.example.springboot.exception.GlobalExceptionHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/*
This class authenticates the JWT token if the "security.enabled" is set to
true in application.properties file. If false then this check is fully bypassed.
This configuration also makes sure that certain APIs like ingestion, login itself (token generation)
and H2 console are authenticated. The IOT sensors would not have information about active JWT tokens
and hence the ingest API should not be authenticated regardless of the "security.enabled" property.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${security.enabled:true}")
    private boolean securityEnabled;

    @Autowired
    private JwtService jwtService;

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        if (!securityEnabled || 
            request.getRequestURI().startsWith("/api/auth") ||
            request.getRequestURI().startsWith("/api/ingest") ||
            request.getRequestURI().startsWith("/h2-console")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token from Authorization header
        String authHeader = request.getHeader("Authorization");

        // If the auth token is null or does not start with Bearer
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("JwtAuthenticationFilter : Missing or invalid Authorization header");
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        // If the auth token is invalid
        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            log.info("JwtAuthenticationFilter : Invalid or expired token");
            SecurityContextHolder.clearContext();
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or expired token");
            return;
        }

        String username = jwtService.extractUsername(token);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username, null, null);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("JwtAuthenticationFilter : Authentication passed");
        filterChain.doFilter(request, response);
    }
}
