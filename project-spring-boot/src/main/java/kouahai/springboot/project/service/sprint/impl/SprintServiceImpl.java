package kouahai.springboot.project.service.sprint.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kouahai.springboot.project.dto.sprint.AddSprintMemberRequest;
import kouahai.springboot.project.dto.sprint.CreateSprintRequest;
import kouahai.springboot.project.dto.project.ProjectMemberResponse;
import kouahai.springboot.project.dto.sprint.SprintResponse;
import kouahai.springboot.project.exception.DuplicateResourceException;
import kouahai.springboot.project.exception.ResourceNotFoundException;
import kouahai.springboot.project.exception.UnauthorizedException;
import kouahai.springboot.project.model.NotificationType;
import kouahai.springboot.project.model.Project;
import kouahai.springboot.project.model.ProjectMember;
import kouahai.springboot.project.model.Sprint;
import kouahai.springboot.project.repository.ProjectMemberRepository;
import kouahai.springboot.project.repository.ProjectRepository;
import kouahai.springboot.project.repository.SprintRepository;
import kouahai.springboot.project.service.auth.AuthorizationService;
import kouahai.springboot.project.service.notification.NotificationService;
import kouahai.springboot.project.service.sprint.SprintService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SprintServiceImpl implements SprintService {
    private final SprintRepository sprintRepository;
    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final AuthorizationService authorizationService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    @CacheEvict(value = "sprints", allEntries = true)
    public SprintResponse createSprint(Long projectId, CreateSprintRequest request, Long userId) {
        log.info("Creating sprint: {} for project: {} by user: {}", request.getName(), projectId, userId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Validate user is Scrum Master of the project
        validateScrumMasterAccess(project, userId);

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // Create sprint
        Sprint sprint = new Sprint();
        sprint.setName(request.getName());
        sprint.setSprintGoal(request.getSprintGoal());
        sprint.setStartDate(request.getStartDate());
        sprint.setEndDate(request.getEndDate());
        sprint.setActive(false); // Sprint is inactive by default
        sprint.setProject(project);

        Sprint savedSprint = sprintRepository.save(sprint);
        log.info("Sprint created with ID: {}", savedSprint.getId());

        // Notify all project members about the new sprint
        notificationService.sendNotificationToProjectMembers(
                project,
                "New Sprint Created",
                "Sprint '" + savedSprint.getName() + "' has been created for project " + project.getName(),
                NotificationType.SPRINT_CREATED,
                savedSprint.getId(),
                "Sprint",
                userId);

        return buildSprintResponse(savedSprint, false);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "sprints", key = "#sprintId")
    public SprintResponse getSprintById(Long sprintId, Long userId) {
        log.info("Getting sprint: {} by user: {}", sprintId, userId);

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        // Validate user is member of the project
        validateUserAccess(sprint.getProject(), userId);

        return buildSprintResponse(sprint, true);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "sprints", key = "'project-' + #projectId")
    public List<SprintResponse> getProjectSprints(Long projectId, Long userId) {
        log.info("Getting all sprints for project: {} by user: {}", projectId, userId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Validate user is member of the project
        validateUserAccess(project, userId);

        List<Sprint> sprints = sprintRepository.findByProjectOrderByStartDateDesc(project);

        return sprints.stream()
                .map(sprint -> buildSprintResponse(sprint, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "sprints", allEntries = true)
    public ProjectMemberResponse addSprintMember(Long sprintId, AddSprintMemberRequest request, Long userId) {
        log.info("Adding member: {} to sprint: {} by user: {}", request.getProjectMemberId(), sprintId, userId);

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        // Validate user is Scrum Master using AuthorizationService
        validateScrumMasterAccess(sprint.getProject(), userId);

        // Get member to add
        ProjectMember memberToAdd = projectMemberRepository.findById(request.getProjectMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Project member not found"));

        // Validate member belongs to the same project
        if (!memberToAdd.getProject().getId().equals(sprint.getProject().getId())) {
            throw new IllegalArgumentException("Member does not belong to this project");
        }

        // Check if member is already in sprint
        if (sprint.getMembers().contains(memberToAdd)) {
            throw new DuplicateResourceException("Member is already in this sprint");
        }

        // Add member to sprint
        sprint.getMembers().add(memberToAdd);
        sprintRepository.save(sprint);

        log.info("Member: {} added to sprint: {}", request.getProjectMemberId(), sprintId);

        return buildMemberResponse(memberToAdd);
    }

    @Override
    @Transactional
    @CacheEvict(value = "sprints", allEntries = true)
    public void removeSprintMember(Long sprintId, Long projectMemberId, Long userId) {
        log.info("Removing member: {} from sprint: {} by user: {}", projectMemberId, sprintId, userId);

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        // Validate user is Scrum Master
        validateScrumMasterAccess(sprint.getProject(), userId);

        // Get member to remove
        ProjectMember memberToRemove = projectMemberRepository.findById(projectMemberId)
                .orElseThrow(() -> new ResourceNotFoundException("Project member not found"));

        // Remove member from sprint
        if (!sprint.getMembers().remove(memberToRemove)) {
            throw new ResourceNotFoundException("Member is not in this sprint");
        }

        sprintRepository.save(sprint);
        log.info("Member: {} removed from sprint: {}", projectMemberId, sprintId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getSprintMembers(Long sprintId, Long userId) {
        log.info("Getting members of sprint: {} by user: {}", sprintId, userId);

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        // Validate user is member of the project
        validateUserAccess(sprint.getProject(), userId);

        return sprint.getMembers().stream()
                .map(this::buildMemberResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectMemberResponse> getAvailableProjectMembers(Long sprintId, Long userId) {
        log.info("Getting available project members for sprint: {} by user: {}", sprintId, userId);

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        // Validate user is member of the project
        validateUserAccess(sprint.getProject(), userId);

        // Get all project members
        List<ProjectMember> allProjectMembers = projectMemberRepository.findByProject(sprint.getProject());

        // Filter out members who are already in the sprint
        return allProjectMembers.stream()
                .filter(member -> !sprint.getMembers().contains(member))
                .map(this::buildMemberResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @CacheEvict(value = "sprints", allEntries = true)
    public SprintResponse activateSprint(Long sprintId, Long userId) {
        log.info("Activating sprint: {} by user: {}", sprintId, userId);

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        // Validate user is Scrum Master
        validateScrumMasterAccess(sprint.getProject(), userId);

        // Check if another sprint is already active
        sprintRepository.findByProjectAndIsActive(sprint.getProject(), true)
                .ifPresent(activeSprint -> {
                    throw new IllegalStateException(
                            "Another sprint is already active. Close it before activating a new one.");
                });

        sprint.setActive(true);
        Sprint savedSprint = sprintRepository.save(sprint);

        log.info("Sprint: {} activated", sprintId);

        // Notify all project members about sprint activation
        notificationService.sendNotificationToProjectMembers(
                savedSprint.getProject(),
                "Sprint Started",
                "Sprint '" + savedSprint.getName() + "' has been activated",
                NotificationType.SPRINT_STARTED,
                savedSprint.getId(),
                "Sprint",
                userId);

        return buildSprintResponse(savedSprint, true);
    }

    @Override
    @Transactional
    @CacheEvict(value = "sprints", allEntries = true)
    public void closeSprint(Long sprintId, Long userId) {
        log.info("Closing sprint: {} by user: {}", sprintId, userId);

        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint not found"));

        // Validate user is Scrum Master
        validateScrumMasterAccess(sprint.getProject(), userId);

        sprint.setActive(false);
        sprintRepository.save(sprint);

        log.info("Sprint: {} closed", sprintId);

        // Notify all project members about sprint closure
        notificationService.sendNotificationToProjectMembers(
                sprint.getProject(),
                "Sprint Closed",
                "Sprint '" + sprint.getName() + "' has been closed",
                NotificationType.SPRINT_CLOSED,
                sprint.getId(),
                "Sprint",
                userId);
    }

    // Helper methods

    private void validateUserAccess(Project project, Long userId) {
        if (!projectMemberRepository.findByProjectAndUserId(project, userId).isPresent()) {
            throw new ResourceNotFoundException("User is not a member of this project");
        }
    }

    private ProjectMember validateScrumMasterAccess(Project project, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectAndUserId(project, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this project"));

        // Use AuthorizationService to validate Scrum Master role
        if (!authorizationService.canManageSprintMembers(member)) {
            throw new UnauthorizedException("Only Scrum Master can manage sprints");
        }

        return member;
    }

    private SprintResponse buildSprintResponse(Sprint sprint, boolean includeMembers) {
        SprintResponse.SprintResponseBuilder builder = SprintResponse.builder()
                .id(sprint.getId())
                .name(sprint.getName())
                .sprintGoal(sprint.getSprintGoal())
                .startDate(sprint.getStartDate())
                .endDate(sprint.getEndDate())
                .isActive(sprint.isActive())
                .membersCount(sprint.getMembers().size());

        if (includeMembers) {
            List<ProjectMemberResponse> members = sprint.getMembers().stream()
                    .map(this::buildMemberResponse)
                    .collect(Collectors.toList());
            builder.members(members);
        }

        return builder.build();
    }

    private ProjectMemberResponse buildMemberResponse(ProjectMember member) {
        return ProjectMemberResponse.builder()
                .id(member.getId())
                .userId(member.getUser().getId())
                .username(member.getUser().getUsername())
                .fullName(member.getUser().getFullName())
                .email(member.getUser().getEmail())
                .role(member.getRole())
                .build();
    }
}
