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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kouahai.springboot.project.dto.userstory.AcceptanceScenarioResponse;
import kouahai.springboot.project.dto.userstory.AddAcceptanceScenarioRequest;
import kouahai.springboot.project.dto.userstory.CreateUserStoryRequest;
import kouahai.springboot.project.dto.userstory.UserStoryResponse;
import kouahai.springboot.project.security.CustomUserDetails;
import kouahai.springboot.project.service.userstory.UserStoryService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user-stories")
@RequiredArgsConstructor
@Tag(name = "User Stories", description = "User story management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class UserStoryController {

    private final UserStoryService userStoryService;

    @PostMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create user story", description = "Create a new user story (Product Owner or Scrum Master)")
    public ResponseEntity<UserStoryResponse> createUserStory(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateUserStoryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserStoryResponse response = userStoryService.createUserStory(projectId, request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userStoryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user story by ID", description = "Get user story details including acceptance scenarios")
    public ResponseEntity<UserStoryResponse> getUserStoryById(
            @PathVariable Long userStoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserStoryResponse response = userStoryService.getUserStoryById(userStoryId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get project user stories", description = "Get all user stories for a project")
    public ResponseEntity<List<UserStoryResponse>> getProjectUserStories(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<UserStoryResponse> userStories = userStoryService.getProjectUserStories(projectId, userDetails.getId());
        return ResponseEntity.ok(userStories);
    }

    @GetMapping("/epic/{epicId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get epic user stories", description = "Get all user stories for an epic")
    public ResponseEntity<List<UserStoryResponse>> getEpicUserStories(
            @PathVariable Long epicId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<UserStoryResponse> userStories = userStoryService.getEpicUserStories(epicId, userDetails.getId());
        return ResponseEntity.ok(userStories);
    }

    @PutMapping("/{userStoryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update user story", description = "Update user story details (Product Owner or Scrum Master)")
    public ResponseEntity<UserStoryResponse> updateUserStory(
            @PathVariable Long userStoryId,
            @Valid @RequestBody CreateUserStoryRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserStoryResponse response = userStoryService.updateUserStory(userStoryId, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{userStoryId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete user story", description = "Delete user story (Product Owner or Scrum Master)")
    public ResponseEntity<Void> deleteUserStory(
            @PathVariable Long userStoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userStoryService.deleteUserStory(userStoryId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userStoryId}/estimate")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Estimate story points", description = "Estimate story points for user story (Scrum Master only)")
    public ResponseEntity<UserStoryResponse> estimateStoryPoints(
            @PathVariable Long userStoryId,
            @RequestParam Integer storyPoints,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        UserStoryResponse response = userStoryService.estimateStoryPoints(userStoryId, storyPoints,
                userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{userStoryId}/acceptance-scenarios")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add acceptance scenario", description = "Add acceptance scenario to user story (Scrum Master only)")
    public ResponseEntity<AcceptanceScenarioResponse> addAcceptanceScenario(
            @PathVariable Long userStoryId,
            @Valid @RequestBody AddAcceptanceScenarioRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        AcceptanceScenarioResponse response = userStoryService.addAcceptanceScenario(userStoryId, request,
                userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/acceptance-scenarios/{scenarioId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete acceptance scenario", description = "Delete acceptance scenario (Scrum Master only)")
    public ResponseEntity<Void> deleteAcceptanceScenario(
            @PathVariable Long scenarioId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userStoryService.deleteAcceptanceScenario(scenarioId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{userStoryId}/acceptance-scenarios")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get acceptance scenarios", description = "Get all acceptance scenarios for a user story")
    public ResponseEntity<List<AcceptanceScenarioResponse>> getAcceptanceScenarios(
            @PathVariable Long userStoryId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<AcceptanceScenarioResponse> scenarios = userStoryService.getAcceptanceScenarios(userStoryId,
                userDetails.getId());
        return ResponseEntity.ok(scenarios);
    }
}
