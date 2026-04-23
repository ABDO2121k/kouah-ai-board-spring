package kouahai.springboot.project.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RolePermissions {

    private boolean canCreateProject;
    private boolean canAddScrumMaster;
    private boolean canManageMembers;
    private boolean canCreateEpics;
    private boolean canCreateUserStories;
    private boolean canAssignEpicsToScrumMaster;
    private boolean canDefineAcceptanceCriteria;
    private boolean canEstimateStoryPoints;
    private boolean canManageBacklog;
    private boolean canPrioritizeStories;
    private boolean canManageSprints;
    private boolean canConfigureKanban;
    private boolean canCreateTasks;
    private boolean canAssignTasks;
    private boolean canUpdateTasks;
    private boolean canMoveTasksInKanban;
    private boolean canMoveToTesterColumns;
    private boolean canCommentOnTasks;
    private boolean canValidateAcceptanceCriteria;
    private boolean canReportBugs;
    private boolean canViewReports;
}
