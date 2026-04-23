package kouahai.springboot.project.service.project;

import java.util.List;

import kouahai.springboot.project.dto.project.AddMemberRequest;
import kouahai.springboot.project.dto.project.CreateProjectRequest;
import kouahai.springboot.project.dto.project.ProjectMemberResponse;
import kouahai.springboot.project.dto.project.ProjectResponse;
import kouahai.springboot.project.dto.project.UpdateProjectRequest;

/**
 * Service for project management operations
 */
public interface ProjectService {

    ProjectResponse createProject(CreateProjectRequest request, Long userId);

    ProjectResponse getProjectById(Long projectId, Long userId);

    ProjectResponse getProjectByKey(String projectKey, Long userId);

    List<ProjectResponse> getAllProjects(Long userId);

    ProjectResponse updateProject(Long projectId, UpdateProjectRequest request, Long userId);

    void deleteProject(Long projectId, Long userId);

    ProjectMemberResponse addMember(Long projectId, AddMemberRequest request, Long userId);

    void removeMember(Long projectId, Long memberId, Long userId);

    List<ProjectMemberResponse> getProjectMembers(Long projectId, Long userId);
}
