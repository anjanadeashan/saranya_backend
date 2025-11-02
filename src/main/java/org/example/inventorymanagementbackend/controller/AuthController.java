package org.example.inventorymanagementbackend.controller;

import org.example.inventorymanagementbackend.dto.request.LoginRequest;
import org.example.inventorymanagementbackend.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.info("Login attempt for username: {}", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtTokenProvider.generateToken(authentication);

            logger.info("Login successful for username: {}", loginRequest.getUsername());

            // Create response with token
            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("type", "Bearer");
            response.put("username", loginRequest.getUsername());
            response.put("message", "Login successful");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Login failed for username: {}", loginRequest.getUsername(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("message", "Invalid username or password");
            errorResponse.put("error", "Authentication failed");

            return ResponseEntity.status(401).body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();

        Map<String, String> response = new HashMap<>();
        response.put("message", "User logged out successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        logger.info("Token validation request received");

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                logger.warn("Invalid or missing Authorization header");
                return ResponseEntity.status(401).body(Map.of("valid", false, "message", "Missing or invalid token"));
            }

            String token = authHeader.substring(7); // Remove "Bearer " prefix
            logger.debug("Validating token: {}...", token.substring(0, Math.min(20, token.length())));

            if (jwtTokenProvider.validateToken(token)) {
                String username = jwtTokenProvider.getUsernameFromToken(token);
                logger.info("Token validated successfully for user: {}", username);

                Map<String, Object> response = new HashMap<>();
                response.put("valid", true);
                response.put("username", username);
                response.put("message", "Token is valid");

                return ResponseEntity.ok(response);
            } else {
                logger.warn("Token validation failed - invalid token");
                return ResponseEntity.status(401).body(Map.of("valid", false, "message", "Invalid token"));
            }

        } catch (Exception e) {
            logger.error("Token validation error: ", e);
            return ResponseEntity.status(401).body(Map.of("valid", false, "message", "Token validation failed"));
        }
    }
}
