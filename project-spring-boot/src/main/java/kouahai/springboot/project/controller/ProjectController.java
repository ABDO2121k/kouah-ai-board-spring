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
import kouahai.springboot.project.dto.project.AddMemberRequest;
import kouahai.springboot.project.dto.project.CreateProjectRequest;
import kouahai.springboot.project.dto.project.ProjectMemberResponse;
import kouahai.springboot.project.dto.project.ProjectResponse;
import kouahai.springboot.project.dto.project.UpdateProjectRequest;
import kouahai.springboot.project.security.CustomUserDetails;
import kouahai.springboot.project.service.project.ProjectService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
@Tag(name = "Projects", description = "Project management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create new project", description = "Creates a new project and assigns creator as Product Owner")
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProjectResponse response = projectService.createProject(request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get all projects", description = "Get all projects where user is a member")
    public ResponseEntity<List<ProjectResponse>> getAllProjects(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ProjectResponse> projects = projectService.getAllProjects(userDetails.getId());
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get project by ID", description = "Get project details by ID")
    public ResponseEntity<ProjectResponse> getProjectById(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProjectResponse response = projectService.getProjectById(projectId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/key/{projectKey}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get project by key", description = "Get project details by project key")
    public ResponseEntity<ProjectResponse> getProjectByKey(
            @PathVariable String projectKey,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProjectResponse response = projectService.getProjectByKey(projectKey, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update project", description = "Update project details (Product Owner only)")
    public ResponseEntity<ProjectResponse> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProjectResponse response = projectService.updateProject(projectId, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete project", description = "Delete project (Product Owner only)")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.deleteProject(projectId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{projectId}/members")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add member to project", description = "Add member to project (PO can add SM, SM can add Dev/Tester)")
    public ResponseEntity<ProjectMemberResponse> addMember(
            @PathVariable Long projectId,
            @Valid @RequestBody AddMemberRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        ProjectMemberResponse response = projectService.addMember(projectId, request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{projectId}/members/{memberId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove member from project", description = "Remove member from project (PO/SM only)")
    public ResponseEntity<Void> removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        projectService.removeMember(projectId, memberId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{projectId}/members")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get project members", description = "Get all members of the project")
    public ResponseEntity<List<ProjectMemberResponse>> getProjectMembers(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<ProjectMemberResponse> members = projectService.getProjectMembers(projectId, userDetails.getId());
        return ResponseEntity.ok(members);
    }
}
