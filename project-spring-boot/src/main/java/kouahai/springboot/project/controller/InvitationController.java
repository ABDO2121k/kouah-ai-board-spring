package kouahai.springboot.project.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import kouahai.springboot.project.dto.invitation.InvitationResponse;
import kouahai.springboot.project.security.CustomUserDetails;
import kouahai.springboot.project.service.invitation.InvitationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
@Tag(name = "Invitations", description = "Project invitation management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class InvitationController {

    private final InvitationService invitationService;

    @PostMapping("/{token}/accept")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Accept project invitation", description = "Accept a project invitation and join the project")
    public ResponseEntity<InvitationResponse> acceptInvitation(
            @PathVariable String token,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        InvitationResponse response = invitationService.acceptInvitation(token, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{token}/deny")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Deny project invitation", description = "Deny a project invitation")
    public ResponseEntity<InvitationResponse> denyInvitation(
            @PathVariable String token,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        InvitationResponse response = invitationService.denyInvitation(token, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/pending")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get pending invitations", description = "Get all pending project invitations for current user")
    public ResponseEntity<List<InvitationResponse>> getPendingInvitations(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<InvitationResponse> invitations = invitationService.getUserPendingInvitations(userDetails.getId());
        return ResponseEntity.ok(invitations);
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get project invitations", description = "Get all pending invitations for a project (Product Owner and Scrum Master only)")
    public ResponseEntity<List<InvitationResponse>> getProjectInvitations(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<InvitationResponse> invitations = invitationService.getProjectInvitations(projectId, userDetails.getId());
        return ResponseEntity.ok(invitations);
    }
}
