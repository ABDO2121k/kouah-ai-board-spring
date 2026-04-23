package kouahai.springboot.project.service.kanban.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kouahai.springboot.project.dto.kanban.CreateKanbanColumnRequest;
import kouahai.springboot.project.dto.kanban.KanbanColumnResponse;
import kouahai.springboot.project.dto.kanban.UpdateKanbanColumnRequest;
import kouahai.springboot.project.exception.DuplicateResourceException;
import kouahai.springboot.project.exception.ResourceNotFoundException;
import kouahai.springboot.project.exception.UnauthorizedException;
import kouahai.springboot.project.model.KanbanColumn;
import kouahai.springboot.project.model.Project;
import kouahai.springboot.project.model.ProjectMember;
import kouahai.springboot.project.model.ProjectRole;
import kouahai.springboot.project.model.Sprint;
import kouahai.springboot.project.repository.KanbanColumnRepository;
import kouahai.springboot.project.repository.ProjectMemberRepository;
import kouahai.springboot.project.repository.SprintRepository;
import kouahai.springboot.project.service.kanban.KanbanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KanbanServiceImpl implements KanbanService {

    private final KanbanColumnRepository kanbanColumnRepository;
    private final SprintRepository sprintRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    @Transactional
    public KanbanColumnResponse createColumn(Long sprintId, CreateKanbanColumnRequest request, Long userId) {
        log.info("Creating kanban column: {} for sprint: {} by user: {}", request.getName(), sprintId, userId);

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        // Only Scrum Master can create kanban columns
        validateScrumMasterAccess(sprint.getProject(), userId);

        // Check for duplicate column names in the sprint
        List<KanbanColumn> existingColumns = kanbanColumnRepository.findBySprint(sprint);
        boolean nameExists = existingColumns.stream()
                .anyMatch(col -> col.getName().equalsIgnoreCase(request.getName()));

        if (nameExists) {
            throw new DuplicateResourceException(
                    "Column with name '" + request.getName() + "' already exists in this sprint");
        }

        KanbanColumn column = new KanbanColumn();
        column.setName(request.getName());
        column.setPositionIndex(request.getPositionIndex());
        column.setDoneStatus(request.isDoneStatus());
        column.setTesterOnly(request.isTesterOnly());
        column.setColor(request.getColor());
        column.setSprint(sprint);

        KanbanColumn savedColumn = kanbanColumnRepository.save(column);

        log.info("Kanban column created successfully: {}", savedColumn.getName());

        return buildKanbanColumnResponse(savedColumn);
    }

    @Override
    @Transactional(readOnly = true)
    public KanbanColumnResponse getColumnById(Long columnId, Long userId) {
        log.info("Getting kanban column by ID: {} for user: {}", columnId, userId);

        KanbanColumn column = kanbanColumnRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException("Kanban column not found"));

        Project project = column.getSprint().getProject();
        validateUserAccess(project, userId);

        return buildKanbanColumnResponse(column);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KanbanColumnResponse> getSprintColumns(Long sprintId, Long userId) {
        log.info("Getting all kanban columns for sprint: {} by user: {}", sprintId, userId);

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        validateUserAccess(sprint.getProject(), userId);

        List<KanbanColumn> columns = kanbanColumnRepository.findBySprintOrderByPositionIndexAsc(sprint);

        return columns.stream()
                .map(this::buildKanbanColumnResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public KanbanColumnResponse updateColumn(Long columnId, UpdateKanbanColumnRequest request, Long userId) {
        log.info("Updating kanban column: {} by user: {}", columnId, userId);

        KanbanColumn column = kanbanColumnRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException("Kanban column not found"));

        Project project = column.getSprint().getProject();
        validateScrumMasterAccess(project, userId);

        // Update fields if provided
        if (request.getName() != null) {
            // Check for duplicate names
            List<KanbanColumn> existingColumns = kanbanColumnRepository.findBySprint(column.getSprint());
            boolean nameExists = existingColumns.stream()
                    .filter(col -> !col.getId().equals(columnId))
                    .anyMatch(col -> col.getName().equalsIgnoreCase(request.getName()));

            if (nameExists) {
                throw new DuplicateResourceException(
                        "Column with name '" + request.getName() + "' already exists in this sprint");
            }
            column.setName(request.getName());
        }

        if (request.getPositionIndex() != null) {
            column.setPositionIndex(request.getPositionIndex());
        }

        if (request.getIsDoneStatus() != null) {
            column.setDoneStatus(request.getIsDoneStatus());
        }

        if (request.getTesterOnly() != null) {
            column.setTesterOnly(request.getTesterOnly());
        }

        if (request.getColor() != null) {
            column.setColor(request.getColor());
        }

        KanbanColumn updatedColumn = kanbanColumnRepository.save(column);

        log.info("Kanban column updated successfully: {}", updatedColumn.getName());

        return buildKanbanColumnResponse(updatedColumn);
    }

    @Override
    @Transactional
    public void deleteColumn(Long columnId, Long userId) {
        log.info("Deleting kanban column: {} by user: {}", columnId, userId);

        KanbanColumn column = kanbanColumnRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException("Kanban column not found"));

        Project project = column.getSprint().getProject();
        validateScrumMasterAccess(project, userId);

        // Check if column has tasks
        if (column.getTasks() != null && !column.getTasks().isEmpty()) {
            throw new IllegalStateException("Cannot delete column with existing tasks. Move or delete tasks first.");
        }

        kanbanColumnRepository.delete(column);

        log.info("Kanban column deleted successfully");
    }

    @Override
    @Transactional
    public List<KanbanColumnResponse> reorderColumns(Long sprintId, List<Long> columnIds, Long userId) {
        log.info("Reordering kanban columns for sprint: {} by user: {}", sprintId, userId);

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        validateScrumMasterAccess(sprint.getProject(), userId);

        // Update position index for each column
        for (int i = 0; i < columnIds.size(); i++) {
            Long columnId = columnIds.get(i);
            KanbanColumn column = kanbanColumnRepository.findById(columnId)
                    .orElseThrow(() -> new ResourceNotFoundException("Kanban column not found: " + columnId));

            if (!column.getSprint().getId().equals(sprintId)) {
                throw new UnauthorizedException("Column does not belong to this sprint");
            }

            column.setPositionIndex(i);
            kanbanColumnRepository.save(column);
        }

        // Return updated columns
        return getSprintColumns(sprintId, userId);
    }

    // Helper methods

    private void validateUserAccess(Project project, Long userId) {
        projectMemberRepository.findByProjectAndUserId(project, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this project"));
    }

    private void validateScrumMasterAccess(Project project, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectAndUserId(project, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this project"));

        if (member.getRole() != ProjectRole.SCRUM_MASTER) {
            throw new UnauthorizedException("Only Scrum Master can perform this action");
        }
    }

    private KanbanColumnResponse buildKanbanColumnResponse(KanbanColumn column) {
        return KanbanColumnResponse.builder()
                .id(column.getId())
                .name(column.getName())
                .positionIndex(column.getPositionIndex())
                .isDoneStatus(column.isDoneStatus())
                .testerOnly(column.isTesterOnly())
                .color(column.getColor())
                .sprintId(column.getSprint().getId())
                .sprintName(column.getSprint().getName())
                .tasksCount(column.getTasks() != null ? column.getTasks().size() : 0)
                .build();
    }
}
