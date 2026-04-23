package kouahai.springboot.project.service.auth;

import kouahai.springboot.project.dto.common.RolePermissions;
import kouahai.springboot.project.dto.common.ValidationResult;
import kouahai.springboot.project.model.KanbanColumn;
import kouahai.springboot.project.model.ProjectMember;
import kouahai.springboot.project.model.ProjectRole;

/**
 * Service for authorization and permission checking
 */
public interface AuthorizationService {

    boolean canMoveTaskToColumn(ProjectMember member, KanbanColumn targetColumn);

    ValidationResult validateTaskMove(ProjectMember member, KanbanColumn currentColumn, KanbanColumn targetColumn);

    RolePermissions getRolePermissions(ProjectRole role);

    boolean hasRole(ProjectMember member, ProjectRole requiredRole);

    boolean isProductOwner(ProjectMember member);

    boolean isScrumMaster(ProjectMember member);

    ValidationResult canAddMemberWithRole(ProjectMember currentMember, ProjectRole roleToAdd);

    boolean canManageProjectMembers(ProjectMember member);

    boolean canManageSprintMembers(ProjectMember member);
}
