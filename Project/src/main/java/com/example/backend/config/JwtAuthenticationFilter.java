package com.example.backend.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;

public class JwtAuthenticationFilter implements Filter {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Log all incoming requests for debugging
        String path = httpRequest.getRequestURI();
        System.out.println("=== JWT FILTER REQUEST ===");
        System.out.println("Path: " + path);
        System.out.println("Method: " + httpRequest.getMethod());
        System.out.println("Auth Header: " + httpRequest.getHeader("Authorization"));
        
        // Skip authentication for health check and error endpoints
        if (path.equals("/actuator/health") || path.equals("/actuator/info") || path.equals("/error") || path.startsWith("/api/health")) {
            System.out.println("Skipping JWT validation for path: " + path);
            chain.doFilter(request, response);
            return;
        }

        String authHeader = httpRequest.getHeader("Authorization");
        System.out.println("=== JWT AUTHENTICATION FILTER ===");
        System.out.println("Request Path: " + path);
        System.out.println("Request Method: " + httpRequest.getMethod());

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String jwt = authHeader.substring(7);
                System.out.println("JWT token provided, length: " + jwt.length());

                // For group operations, we require a real JWT token
                if (jwt.startsWith("test-token-")) {
                    System.err.println("ERROR: Test tokens are not allowed for group operations");
                    sendUnauthorizedResponse(httpResponse, "Real authentication token required");
                    return;
                }
                
                // Check if this is a real JWT token (starts with eyJ...)
                if (jwt.startsWith("eyJ")) {
                    try {
                        // Decode the JWT token to extract user ID
                        String[] parts = jwt.split("\\.");
                        if (parts.length != 3) {
                            System.out.println("ERROR: Invalid JWT format - expected 3 parts, got: " + parts.length);
                            sendUnauthorizedResponse(httpResponse, "Invalid token format");
                            return;
                        }
                        
                        // Decode the payload
                        String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                        Map<String, Object> claims = objectMapper.readValue(payload, Map.class);
                        
                        // Extract user ID from claims (Supabase uses 'sub')
                        String userId = (String) claims.get("sub");
                        if (userId == null || userId.isEmpty()) {
                            System.out.println("ERROR: No user ID (sub) found in JWT claims");
                            sendUnauthorizedResponse(httpResponse, "No user ID in token");
                            return;
                        }
                        
                        System.out.println("JWT authentication successful for user: " + userId);
                        
                        // Set user ID and authentication status as request attributes
                        System.out.println("Setting request attributes - userId: " + userId + ", authenticated: true");
                        httpRequest.setAttribute("userId", userId);
                        httpRequest.setAttribute("authenticated", true);
                        httpRequest.setAttribute("authenticationMissing", false);
                        
                        // Log the attributes to verify they're being set correctly
                        System.out.println("Request attributes after setting:");
                        System.out.println("  userId: " + httpRequest.getAttribute("userId"));
                        System.out.println("  authenticated: " + httpRequest.getAttribute("authenticated"));
                        System.out.println("  authenticationMissing: " + httpRequest.getAttribute("authenticationMissing"));
                        
                        // Enhanced logging for JWT token validation
                        System.out.println("JWT Token Validation:");
                        System.out.println("  Token: " + jwt);
                        System.out.println("  Payload: " + payload);
                        System.out.println("  Claims: " + claims);
                        
                        // Set up Spring Security context
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                            userId, 
                            null, 
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                        authentication.setDetails(claims);
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        System.out.println("Security context set with authentication for user: " + userId);
                        
                        // Continue with the filter chain
                        chain.doFilter(request, response);
                        return;
                        
                    } catch (Exception e) {
                        System.out.println("ERROR processing JWT token: " + e.getMessage());
                        e.printStackTrace();
                        sendUnauthorizedResponse(httpResponse, "Invalid token");
                        return;
                    }
                }
                
                if (jwt.equals("null") || jwt.isEmpty()) {
                    System.out.println("ERROR: Empty or null token provided");
                    sendUnauthorizedResponse(httpResponse, "Valid authentication token required");
                    return;
                }

                try {
                    // Debug: Print the raw JWT token (first 10 chars for security)
                    System.out.println("DEBUG: Raw JWT (first 50 chars): " + (jwt.length() > 50 ? jwt.substring(0, 50) + "..." : jwt));
                    
                    // Safely decode JWT parts - handle both standard and URL-encoded tokens
                    String[] parts = jwt.split("\\.");
                    System.out.println("DEBUG: JWT parts length: " + parts.length);
                    
                    if (parts.length != 3) {
                        // Try URL decoding first in case the token is URL-encoded
                        String decodedJwt = java.net.URLDecoder.decode(jwt, "UTF-8");
                        System.out.println("DEBUG: URL-decoded JWT: " + (decodedJwt.length() > 50 ? decodedJwt.substring(0, 50) + "..." : decodedJwt));
                        parts = decodedJwt.split("\\.");
                        System.out.println("DEBUG: After URL decode, JWT parts length: " + parts.length);
                        
                        if (parts.length != 3) {
                            System.out.println("ERROR: Invalid JWT format - expected 3 parts, got: " + parts.length);
                            System.out.println("DEBUG: JWT parts: " + java.util.Arrays.toString(parts));
                            sendUnauthorizedResponse(httpResponse, "Invalid token format");
                            return;
                        }
                    }

                    // Safely decode payload
                    try {
                        // Add padding if needed for Base64 URL decoding
                        String payload = parts[1];
                        switch (payload.length() % 4) {
                            case 2: payload += "=="; break;
                            case 3: payload += "="; break;
                        }
                        
                        payload = new String(Base64.getUrlDecoder().decode(payload));
                        System.out.println("JWT Payload: " + payload);
                        
                        @SuppressWarnings("unchecked")
                        Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

                        if (claims == null || claims.isEmpty()) {
                            System.out.println("ERROR: No claims found in JWT");
                            sendUnauthorizedResponse(httpResponse, "Invalid token claims");
                            return;
                        }

                        System.out.println("JWT Claims: " + claims);
                        
                        // Extract user ID from claims - handle different claim formats
                        String userId = null;
                        if (claims.containsKey("sub")) {
                            // Supabase uses 'sub' as the user ID
                            userId = claims.get("sub").toString();
                            System.out.println("Found user ID in 'sub' claim: " + userId);
                        } else if (claims.containsKey("user_id")) {
                            // Fallback to 'user_id' if present
                            userId = claims.get("user_id").toString();
                            System.out.println("Found user ID in 'user_id' claim: " + userId);
                        } else if (claims.containsKey("email")) {
                            // For development, use email as fallback
                            userId = claims.get("email").toString();
                            System.out.println("Using email as user ID: " + userId);
                        }
                        
                        if (userId == null || userId.isEmpty()) {
                            System.out.println("ERROR: No user ID found in JWT claims");
                            sendUnauthorizedResponse(httpResponse, "No user ID in token");
                            return;
                        }

                        httpRequest.setAttribute("userId", userId);
                        httpRequest.setAttribute("authenticated", true);
                        System.out.println("SUCCESS: Set user ID from JWT: " + userId);
                        
                    } catch (Exception decodeError) {
                        System.out.println("ERROR: Error decoding JWT payload: " + decodeError.getMessage());
                        sendUnauthorizedResponse(httpResponse, "Token decode error");
                        return;
                    }
                } catch (Exception jwtError) {
                    System.out.println("ERROR: Error processing JWT: " + jwtError.getMessage());
                    sendUnauthorizedResponse(httpResponse, "Token processing error");
                    return;
                }
            } else {
                System.out.println("WARNING: No Authorization header found");
                // Instead of immediately failing, set a flag that authentication is missing
                // The controllers can decide how to handle this
                httpRequest.setAttribute("authenticated", false);
                httpRequest.setAttribute("authenticationMissing", true);
                System.out.println("INFO: Request marked as unauthenticated - controllers will handle appropriately");
            }

            System.out.println("SUCCESS: Authentication processing completed");
            System.out.println("User ID: " + httpRequest.getAttribute("userId"));
            System.out.println("Authenticated: " + httpRequest.getAttribute("authenticated"));
            System.out.println("=== END JWT AUTHENTICATION FILTER ===");
            
            // Continue with request (authenticated or not)
            chain.doFilter(request, response);
            
        } catch (Exception filterError) {
            System.err.println("CRITICAL ERROR in JWT Authentication Filter:");
            System.err.println("Error message: " + filterError.getMessage());
            System.err.println("Error class: " + filterError.getClass().getName());
            filterError.printStackTrace();
            
            try {
                httpResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                httpResponse.getWriter().write("{\"error\":\"Authentication filter error\"}");
            } catch (Exception writeError) {
                System.err.println("CRITICAL: Failed to write error response: " + writeError.getMessage());
            }
        }
    }

    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
