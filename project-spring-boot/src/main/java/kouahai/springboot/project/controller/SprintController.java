package kouahai.springboot.project.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kouahai.springboot.project.dto.sprint.AddSprintMemberRequest;
import kouahai.springboot.project.dto.sprint.CreateSprintRequest;
import kouahai.springboot.project.dto.project.ProjectMemberResponse;
import kouahai.springboot.project.dto.sprint.SprintResponse;
import kouahai.springboot.project.security.CustomUserDetails;
import kouahai.springboot.project.service.sprint.SprintService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/sprints")
@RequiredArgsConstructor
@Tag(name = "Sprints", description = "Sprint management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class SprintController {

    private final SprintService sprintService;

    @PostMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create new sprint", description = "Creates a new sprint for a project (Scrum Master only)")
    public ResponseEntity<SprintResponse> createSprint(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateSprintRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        SprintResponse response = sprintService.createSprint(projectId, request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{sprintId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get sprint by ID", description = "Get sprint details by ID")
    public ResponseEntity<SprintResponse> getSprintById(
            @PathVariable Long sprintId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        SprintResponse response = sprintService.getSprintById(sprintId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get project sprints", description = "Get all sprints for a project")
    public ResponseEntity<List<SprintResponse>> getProjectSprints(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<SprintResponse> sprints = sprintService.getProjectSprints(projectId, userDetails.getId());
        return ResponseEntity.ok(sprints);
    }

    @PostMapping("/{sprintId}/members")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add member to sprint", description = "Add project member to sprint (Scrum Master only)")
    public ResponseEntity<ProjectMemberResponse> addSprintMember(
            @PathVariable Long sprintId,
            @Valid @RequestBody AddSprintMemberRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProjectMemberResponse response = sprintService.addSprintMember(sprintId, request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{sprintId}/members/{projectMemberId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove member from sprint", description = "Remove member from sprint (Scrum Master only)")
    public ResponseEntity<Void> removeSprintMember(
            @PathVariable Long sprintId,
            @PathVariable Long projectMemberId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        sprintService.removeSprintMember(sprintId, projectMemberId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{sprintId}/members")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get sprint members", description = "Get all members of the sprint")
    public ResponseEntity<List<ProjectMemberResponse>> getSprintMembers(
            @PathVariable Long sprintId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ProjectMemberResponse> members = sprintService.getSprintMembers(sprintId, userDetails.getId());
        return ResponseEntity.ok(members);
    }

    @GetMapping("/{sprintId}/available-members")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get available project members", description = "Get project members who are not yet in the sprint (for adding to sprint)")
    public ResponseEntity<List<ProjectMemberResponse>> getAvailableProjectMembers(
            @PathVariable Long sprintId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ProjectMemberResponse> members = sprintService.getAvailableProjectMembers(sprintId, userDetails.getId());
        return ResponseEntity.ok(members);
    }

    @PutMapping("/{sprintId}/activate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Activate sprint", description = "Activate a sprint (Scrum Master only)")
    public ResponseEntity<SprintResponse> activateSprint(
            @PathVariable Long sprintId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        SprintResponse response = sprintService.activateSprint(sprintId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{sprintId}/close")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Close sprint", description = "Close a sprint (Scrum Master only)")
    public ResponseEntity<Void> closeSprint(
            @PathVariable Long sprintId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        sprintService.closeSprint(sprintId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
