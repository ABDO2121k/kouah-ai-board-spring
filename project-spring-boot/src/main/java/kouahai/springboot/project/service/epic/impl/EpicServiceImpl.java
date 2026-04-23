package kouahai.springboot.project.service.epic.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kouahai.springboot.project.dto.epic.CreateEpicRequest;
import kouahai.springboot.project.dto.epic.EpicResponse;
import kouahai.springboot.project.dto.epic.UpdateEpicRequest;
import kouahai.springboot.project.exception.ResourceNotFoundException;
import kouahai.springboot.project.exception.UnauthorizedException;
import kouahai.springboot.project.model.Epic;
import kouahai.springboot.project.model.ProductBacklog;
import kouahai.springboot.project.model.Project;
import kouahai.springboot.project.model.ProjectMember;
import kouahai.springboot.project.model.ProjectRole;
import kouahai.springboot.project.repository.EpicRepository;
import kouahai.springboot.project.repository.ProductBacklogRepository;
import kouahai.springboot.project.repository.ProjectMemberRepository;
import kouahai.springboot.project.repository.ProjectRepository;
import kouahai.springboot.project.service.epic.EpicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EpicServiceImpl implements EpicService {

    private final EpicRepository epicRepository;
    private final ProjectRepository projectRepository;
    private final ProductBacklogRepository productBacklogRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    @Transactional
    public EpicResponse createEpic(Long projectId, CreateEpicRequest request, Long userId) {
        log.info("Creating epic for project: {} by user: {}", projectId, userId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Only Product Owner can create epics
        validateProductOwnerAccess(project, userId);

        // Get product backlog for this project
        ProductBacklog backlog = productBacklogRepository.findByProject(project)
                .orElseThrow(() -> new ResourceNotFoundException("Product backlog not found for project"));

        Epic epic = new Epic();
        epic.setTitle(request.getTitle());
        epic.setDescription(request.getDescription());
        epic.setProductBacklog(backlog);

        Epic savedEpic = epicRepository.save(epic);

        log.info("Epic created successfully: {}", savedEpic.getTitle());

        return buildEpicResponse(savedEpic);
    }

    @Override
    @Transactional(readOnly = true)
    public EpicResponse getEpicById(Long epicId, Long userId) {
        log.info("Getting epic by ID: {} for user: {}", epicId, userId);

        Epic epic = epicRepository.findById(epicId)
                .orElseThrow(() -> new ResourceNotFoundException("Epic not found"));

        // Validate user has access to the project
        Project project = epic.getProductBacklog().getProject();
        validateUserAccess(project, userId);

        return buildEpicResponse(epic);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EpicResponse> getProjectEpics(Long projectId, Long userId) {
        log.info("Getting all epics for project: {} by user: {}", projectId, userId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        validateUserAccess(project, userId);

        ProductBacklog backlog = productBacklogRepository.findByProject(project)
                .orElseThrow(() -> new ResourceNotFoundException("Product backlog not found for project"));

        List<Epic> epics = epicRepository.findByProductBacklog(backlog);

        return epics.stream()
                .map(this::buildEpicResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EpicResponse updateEpic(Long epicId, UpdateEpicRequest request, Long userId) {
        log.info("Updating epic: {} by user: {}", epicId, userId);

        Epic epic = epicRepository.findById(epicId)
                .orElseThrow(() -> new ResourceNotFoundException("Epic not found"));

        Project project = epic.getProductBacklog().getProject();
        validateProductOwnerAccess(project, userId);

        epic.setTitle(request.getTitle());
        epic.setDescription(request.getDescription());

        Epic updatedEpic = epicRepository.save(epic);

        log.info("Epic updated successfully: {}", updatedEpic.getTitle());

        return buildEpicResponse(updatedEpic);
    }

    @Override
    @Transactional
    public void deleteEpic(Long epicId, Long userId) {
        log.info("Deleting epic: {} by user: {}", epicId, userId);

        Epic epic = epicRepository.findById(epicId)
                .orElseThrow(() -> new ResourceNotFoundException("Epic not found"));

        Project project = epic.getProductBacklog().getProject();
        validateProductOwnerAccess(project, userId);

        epicRepository.delete(epic);

        log.info("Epic deleted successfully");
    }

    // Helper methods

    private void validateUserAccess(Project project, Long userId) {
        projectMemberRepository.findByProjectAndUserId(project, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this project"));
    }

    private void validateProductOwnerAccess(Project project, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectAndUserId(project, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this project"));

        if (member.getRole() != ProjectRole.PRODUCT_OWNER) {
            throw new UnauthorizedException("Only Product Owner can perform this action");
        }
    }

    private EpicResponse buildEpicResponse(Epic epic) {
        return EpicResponse.builder()
                .id(epic.getId())
                .title(epic.getTitle())
                .description(epic.getDescription())
                .productBacklogId(epic.getProductBacklog().getId())
                .projectId(epic.getProductBacklog().getProject().getId())
                .projectName(epic.getProductBacklog().getProject().getName())
                .userStoriesCount(epic.getUserStories() != null ? epic.getUserStories().size() : 0)
                .build();
    }
}
