package kouahai.springboot.project.service.auth.impl;

import org.springframework.stereotype.Service;

import kouahai.springboot.project.dto.common.RolePermissions;
import kouahai.springboot.project.dto.common.ValidationResult;
import kouahai.springboot.project.model.KanbanColumn;
import kouahai.springboot.project.model.ProjectMember;
import kouahai.springboot.project.model.ProjectRole;
import kouahai.springboot.project.service.auth.AuthorizationService;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to handle role-based access control for Kanban board operations
 */
@Service
@Slf4j
public class AuthorizationServiceImpl implements AuthorizationService {

    @Override
    public boolean canMoveTaskToColumn(ProjectMember member, KanbanColumn targetColumn) {
        if (member == null || targetColumn == null) {
            log.warn("Null member or target column provided");
            return false;
        }

        ProjectRole role = member.getRole();

        // If column is marked as testerOnly, only TESTER role can move tasks there
        if (targetColumn.isTesterOnly()) {
            boolean canMove = role == ProjectRole.TESTER;
            log.debug("Tester-only column: {} - User role: {} - Can move: {}",
                    targetColumn.getName(), role, canMove);
            return canMove;
        }

        // All team members can move tasks to non-restricted columns
        boolean canMove = role == ProjectRole.DEVELOPER
                || role == ProjectRole.TESTER
                || role == ProjectRole.SCRUM_MASTER;

        log.debug("User role: {} - Can move to column '{}': {}", role, targetColumn.getName(), canMove);
        return canMove;
    }

    @Override
    public ValidationResult validateTaskMove(ProjectMember member,
            KanbanColumn currentColumn,
            KanbanColumn targetColumn) {

        log.debug("Validating task move for member: {} from column: {} to column: {}",
                member.getUser().getUsername(),
                currentColumn != null ? currentColumn.getName() : "null",
                targetColumn.getName());

        // Check if user can move to target column
        if (!canMoveTaskToColumn(member, targetColumn)) {
            String error = String.format("Only TESTER role can move tasks to '%s' column",
                    targetColumn.getName());
            log.warn("Task move validation failed: {}", error);
            return ValidationResult.error(error);
        }

        // Additional business rules
        if (currentColumn != null && targetColumn != null) {
            if (!currentColumn.getSprint().getId().equals(targetColumn.getSprint().getId())) {
                String error = "Cannot move task between different sprints";
                log.warn("Task move validation failed: {}", error);
                return ValidationResult.error(error);
            }
        }

        log.debug("Task move validation successful");
        return ValidationResult.success();
    }

    @Override
    public RolePermissions getRolePermissions(ProjectRole role) {
        log.debug("Getting permissions for role: {}", role);

        RolePermissions.RolePermissionsBuilder builder = RolePermissions.builder();

        switch (role) {
            case PRODUCT_OWNER:
                return builder
                        .canCreateProject(true)
                        .canAddScrumMaster(true)
                        .canCreateEpics(true)
                        .canCreateUserStories(true)
                        .canAssignEpicsToScrumMaster(true)
                        .canPrioritizeStories(true)
                        .canViewReports(true)
                        .canManageMembers(false)
                        .canManageSprints(false)
                        .canMoveToTesterColumns(false)
                        .build();

            case SCRUM_MASTER:
                return builder
                        .canManageMembers(true)
                        .canCreateUserStories(true)
                        .canDefineAcceptanceCriteria(true)
                        .canEstimateStoryPoints(true)
                        .canManageBacklog(true)
                        .canManageSprints(true)
                        .canConfigureKanban(true)
                        .canCreateTasks(true)
                        .canAssignTasks(true)
                        .canViewReports(true)
                        .canMoveToTesterColumns(false)
                        .build();

            case DEVELOPER:
                return builder
                        .canCreateTasks(true)
                        .canUpdateTasks(true)
                        .canMoveTasksInKanban(true)
                        .canCommentOnTasks(true)
                        .canMoveToTesterColumns(false)
                        .build();

            case TESTER:
                return builder
                        .canCreateTasks(true)
                        .canUpdateTasks(true)
                        .canMoveTasksInKanban(true)
                        .canCommentOnTasks(true)
                        .canMoveToTesterColumns(true)
                        .canValidateAcceptanceCriteria(true)
                        .canReportBugs(true)
                        .build();

            default:
                log.warn("Unknown role: {}, returning empty permissions", role);
                return builder.build();
        }
    }

    @Override
    public boolean hasRole(ProjectMember member, ProjectRole requiredRole) {
        if (member == null || requiredRole == null) {
            log.warn("Null member or required role provided");
            return false;
        }
        boolean hasRole = member.getRole() == requiredRole;
        log.debug("Member {} has role {}: {}", member.getUser().getUsername(), requiredRole, hasRole);
        return hasRole;
    }

    @Override
    public boolean isProductOwner(ProjectMember member) {
        return hasRole(member, ProjectRole.PRODUCT_OWNER);
    }

    @Override
    public boolean isScrumMaster(ProjectMember member) {
        return hasRole(member, ProjectRole.SCRUM_MASTER);
    }

    @Override
    public ValidationResult canAddMemberWithRole(ProjectMember currentMember, ProjectRole roleToAdd) {
        if (currentMember == null) {
            String error = "Current member cannot be null";
            log.warn(error);
            return ValidationResult.error(error);
        }

        if (roleToAdd == null) {
            String error = "Role to add cannot be null";
            log.warn(error);
            return ValidationResult.error(error);
        }

        ProjectRole currentRole = currentMember.getRole();
        log.debug("Validating if {} can add member with role {}", currentRole, roleToAdd);

        // Product Owner can only add Scrum Master
        if (currentRole == ProjectRole.PRODUCT_OWNER) {
            if (roleToAdd == ProjectRole.SCRUM_MASTER) {
                log.debug("Product Owner can add Scrum Master - validation passed");
                return ValidationResult.success();
            } else {
                String error = "Product Owner can only add Scrum Master to the project";
                log.warn(error);
                return ValidationResult.error(error);
            }
        }

        // Scrum Master can add Developer or Tester
        if (currentRole == ProjectRole.SCRUM_MASTER) {
            if (roleToAdd == ProjectRole.DEVELOPER || roleToAdd == ProjectRole.TESTER) {
                log.debug("Scrum Master can add {} - validation passed", roleToAdd);
                return ValidationResult.success();
            } else {
                String error = "Scrum Master can only add Developer or Tester to the project";
                log.warn(error);
                return ValidationResult.error(error);
            }
        }

        // Developers and Testers cannot add members
        String error = String.format("Role %s does not have permission to add members to the project", currentRole);
        log.warn(error);
        return ValidationResult.error(error);
    }

    @Override
    public boolean canManageProjectMembers(ProjectMember member) {
        if (member == null) {
            log.warn("Null member provided");
            return false;
        }

        ProjectRole role = member.getRole();
        boolean canManage = role == ProjectRole.PRODUCT_OWNER || role == ProjectRole.SCRUM_MASTER;
        log.debug("Member with role {} can manage project members: {}", role, canManage);
        return canManage;
    }

    @Override
    public boolean canManageSprintMembers(ProjectMember member) {
        if (member == null) {
            log.warn("Null member provided");
            return false;
        }

        boolean canManage = isScrumMaster(member);
        log.debug("Member can manage sprint members: {}", canManage);
        return canManage;
    }
}
