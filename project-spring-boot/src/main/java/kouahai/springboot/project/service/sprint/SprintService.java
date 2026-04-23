package kouahai.springboot.project.service.sprint;

import java.util.List;

import kouahai.springboot.project.dto.sprint.AddSprintMemberRequest;
import kouahai.springboot.project.dto.sprint.CreateSprintRequest;
import kouahai.springboot.project.dto.project.ProjectMemberResponse;
import kouahai.springboot.project.dto.sprint.SprintResponse;

public interface SprintService {

    SprintResponse createSprint(Long projectId, CreateSprintRequest request, Long userId);

    SprintResponse getSprintById(Long sprintId, Long userId);

    List<SprintResponse> getProjectSprints(Long projectId, Long userId);

    ProjectMemberResponse addSprintMember(Long sprintId, AddSprintMemberRequest request, Long userId);

    void removeSprintMember(Long sprintId, Long projectMemberId, Long userId);

    List<ProjectMemberResponse> getSprintMembers(Long sprintId, Long userId);

    List<ProjectMemberResponse> getAvailableProjectMembers(Long sprintId, Long userId);

    SprintResponse activateSprint(Long sprintId, Long userId);

    void closeSprint(Long sprintId, Long userId);
}
