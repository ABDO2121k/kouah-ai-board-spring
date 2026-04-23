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
import kouahai.springboot.project.dto.epic.CreateEpicRequest;
import kouahai.springboot.project.dto.epic.EpicResponse;
import kouahai.springboot.project.dto.epic.UpdateEpicRequest;
import kouahai.springboot.project.security.CustomUserDetails;
import kouahai.springboot.project.service.epic.EpicService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/epics")
@RequiredArgsConstructor
@Tag(name = "Epics", description = "Epic management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class EpicController {

    private final EpicService epicService;

    @PostMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create epic", description = "Create a new epic in product backlog (Product Owner only)")
    public ResponseEntity<EpicResponse> createEpic(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateEpicRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        EpicResponse response = epicService.createEpic(projectId, request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{epicId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get epic by ID", description = "Get epic details by ID")
    public ResponseEntity<EpicResponse> getEpicById(
            @PathVariable Long epicId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        EpicResponse response = epicService.getEpicById(epicId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/project/{projectId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get project epics", description = "Get all epics for a project")
    public ResponseEntity<List<EpicResponse>> getProjectEpics(
            @PathVariable Long projectId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<EpicResponse> epics = epicService.getProjectEpics(projectId, userDetails.getId());
        return ResponseEntity.ok(epics);
    }

    @PutMapping("/{epicId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update epic", description = "Update epic details (Product Owner only)")
    public ResponseEntity<EpicResponse> updateEpic(
            @PathVariable Long epicId,
            @Valid @RequestBody UpdateEpicRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        EpicResponse response = epicService.updateEpic(epicId, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{epicId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete epic", description = "Delete epic (Product Owner only)")
    public ResponseEntity<Void> deleteEpic(
            @PathVariable Long epicId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        epicService.deleteEpic(epicId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
