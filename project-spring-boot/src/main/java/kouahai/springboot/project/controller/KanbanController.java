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
import kouahai.springboot.project.dto.kanban.CreateKanbanColumnRequest;
import kouahai.springboot.project.dto.kanban.KanbanColumnResponse;
import kouahai.springboot.project.dto.kanban.UpdateKanbanColumnRequest;
import kouahai.springboot.project.security.CustomUserDetails;
import kouahai.springboot.project.service.kanban.KanbanService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/kanban")
@RequiredArgsConstructor
@Tag(name = "Kanban Board", description = "Kanban board column management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class KanbanController {

    private final KanbanService kanbanService;

    @PostMapping("/sprint/{sprintId}/columns")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Create kanban column", description = "Create a new kanban column for a sprint (Scrum Master only)")
    public ResponseEntity<KanbanColumnResponse> createColumn(
            @PathVariable Long sprintId,
            @Valid @RequestBody CreateKanbanColumnRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        KanbanColumnResponse response = kanbanService.createColumn(sprintId, request, userDetails.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/columns/{columnId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get kanban column", description = "Get kanban column details by ID")
    public ResponseEntity<KanbanColumnResponse> getColumnById(
            @PathVariable Long columnId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        KanbanColumnResponse response = kanbanService.getColumnById(columnId, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sprint/{sprintId}/columns")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get sprint kanban columns", description = "Get all kanban columns for a sprint ordered by position")
    public ResponseEntity<List<KanbanColumnResponse>> getSprintColumns(
            @PathVariable Long sprintId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<KanbanColumnResponse> columns = kanbanService.getSprintColumns(sprintId, userDetails.getId());
        return ResponseEntity.ok(columns);
    }

    @PutMapping("/columns/{columnId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update kanban column", description = "Update kanban column details (Scrum Master only)")
    public ResponseEntity<KanbanColumnResponse> updateColumn(
            @PathVariable Long columnId,
            @Valid @RequestBody UpdateKanbanColumnRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        KanbanColumnResponse response = kanbanService.updateColumn(columnId, request, userDetails.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/columns/{columnId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Delete kanban column", description = "Delete kanban column (Scrum Master only)")
    public ResponseEntity<Void> deleteColumn(
            @PathVariable Long columnId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        kanbanService.deleteColumn(columnId, userDetails.getId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/sprint/{sprintId}/columns/reorder")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Reorder kanban columns", description = "Reorder kanban columns by providing list of column IDs (Scrum Master only)")
    public ResponseEntity<List<KanbanColumnResponse>> reorderColumns(
            @PathVariable Long sprintId,
            @RequestBody List<Long> columnIds,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        List<KanbanColumnResponse> columns = kanbanService.reorderColumns(sprintId, columnIds, userDetails.getId());
        return ResponseEntity.ok(columns);
    }
}
