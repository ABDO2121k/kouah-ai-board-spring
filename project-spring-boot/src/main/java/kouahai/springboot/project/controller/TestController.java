package kouahai.springboot.project.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kouahai.springboot.project.security.CustomUserDetails;

@RestController
@RequestMapping("/api/test")
@Tag(name = "Test Endpoints", description = "Test endpoints for authentication and role-based access")
public class TestController {

    @GetMapping("/public")
    @Operation(summary = "Public endpoint", description = "Accessible without authentication")
    public ResponseEntity<String> publicEndpoint() {
        return ResponseEntity.ok("Public endpoint - No authentication required");
    }

    @GetMapping("/authenticated")
    @Operation(summary = "Authenticated endpoint", description = "Requires authentication")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<String> authenticatedEndpoint() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        return ResponseEntity.ok(
                "Authenticated endpoint - Welcome " + userDetails.getUsername() +
                        "! Roles: " + userDetails.getAuthorities());
    }
}
