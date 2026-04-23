package kouahai.springboot.project.service.project.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kouahai.springboot.project.dto.project.AddMemberRequest;
import kouahai.springboot.project.dto.project.CreateProjectRequest;
import kouahai.springboot.project.dto.project.ProjectMemberResponse;
import kouahai.springboot.project.dto.project.ProjectResponse;
import kouahai.springboot.project.dto.project.UpdateProjectRequest;
import kouahai.springboot.project.dto.common.ValidationResult;
import kouahai.springboot.project.exception.DuplicateResourceException;
import kouahai.springboot.project.exception.ResourceNotFoundException;
import kouahai.springboot.project.exception.UnauthorizedException;
import kouahai.springboot.project.model.InvitationStatus;
import kouahai.springboot.project.model.ProductBacklog;
import kouahai.springboot.project.model.Project;
import kouahai.springboot.project.model.ProjectInvitation;
import kouahai.springboot.project.model.ProjectMember;
import kouahai.springboot.project.model.ProjectRole;
import kouahai.springboot.project.model.User;
import kouahai.springboot.project.model.UserStatus;
import kouahai.springboot.project.repository.ProductBacklogRepository;
import kouahai.springboot.project.repository.ProjectInvitationRepository;
import kouahai.springboot.project.repository.ProjectMemberRepository;
import kouahai.springboot.project.repository.ProjectRepository;
import kouahai.springboot.project.repository.UserRepository;
import kouahai.springboot.project.service.auth.AuthorizationService;
import kouahai.springboot.project.service.email.EmailService;
import kouahai.springboot.project.service.notification.NotificationPublisher;
import kouahai.springboot.project.service.project.ProjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProductBacklogRepository productBacklogRepository;
    private final ProjectInvitationRepository projectInvitationRepository;
    private final AuthorizationService authorizationService;
    private final NotificationPublisher notificationPublisher;
    private final EmailService emailService;

    @Override
    @Transactional
    public ProjectResponse createProject(CreateProjectRequest request, Long userId) {
        log.info("Creating project with key: {} by user: {}", request.getProjectKey(), userId);

        // Validate project key uniqueness
        if (projectRepository.existsByKey(request.getProjectKey())) {
            throw new DuplicateResourceException("Project key already exists: " + request.getProjectKey());
        }

        if (projectRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Project name already exists: " + request.getName());
        }

        // Get user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Create project
        Project project = new Project();
        project.setKey(request.getProjectKey());
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());

        Project savedProject = projectRepository.save(project);

        // Create product backlog for the project
        ProductBacklog backlog = new ProductBacklog();
        backlog.setProject(savedProject);
        productBacklogRepository.save(backlog);

        // Assign creator as Product Owner
        ProjectMember productOwner = new ProjectMember();
        productOwner.setProject(savedProject);
        productOwner.setUser(user);
        productOwner.setRole(ProjectRole.PRODUCT_OWNER);
        productOwner.setJoinedAt(LocalDateTime.now());
        projectMemberRepository.save(productOwner);

        log.info("Project created successfully: {} with Product Owner: {}", savedProject.getKey(), user.getUsername());

        return buildProjectResponse(savedProject, true);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "#projectId")
    public ProjectResponse getProjectById(Long projectId, Long userId) {
        log.info("Getting project by ID: {} for user: {}", projectId, userId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        validateUserAccess(project, userId);

        return buildProjectResponse(project, true);
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projects", key = "#projectKey")
    public ProjectResponse getProjectByKey(String projectKey, Long userId) {
        log.info("Getting project by key: {} for user: {}", projectKey, userId);
        Project project = projectRepository.findByKey(projectKey)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with key: " + projectKey));

        validateUserAccess(project, userId);

        return buildProjectResponse(project, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectResponse> getAllProjects(Long userId) {
        log.info("Getting all projects for user: {}", userId);

        List<ProjectMember> memberships = projectMemberRepository.findByUserId(userId);

        return memberships.stream()
                .map(ProjectMember::getProject)
                .map(project -> buildProjectResponse(project, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#projectId"),
            @CacheEvict(value = "projects", allEntries = true)
    })
    public ProjectResponse updateProject(Long projectId, UpdateProjectRequest request, Long userId) {
        log.info("Updating project: {} by user: {}", projectId, userId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        validateProductOwnerAccess(project, userId);

        // Check name uniqueness if changed
        if (!project.getName().equals(request.getName()) && projectRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("Project name already exists: " + request.getName());
        }

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setUpdatedAt(LocalDateTime.now());

        Project updatedProject = projectRepository.save(project);

        log.info("Project updated successfully: {}", updatedProject.getKey());

        return buildProjectResponse(updatedProject, true);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "projects", key = "#projectId"),
            @CacheEvict(value = "projects", allEntries = true),
            @CacheEvict(value = "projectMembers", allEntries = true)
    })
    public void deleteProject(Long projectId, Long userId) {
        log.info("Deleting project: {} by user: {}", projectId, userId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        validateProductOwnerAccess(project, userId);

        projectRepository.delete(project);

        log.info("Project deleted successfully: {}", project.getKey());
    }

    @Override
    @Transactional
    @CacheEvict(value = "projectMembers", key = "#projectId")
    public ProjectMemberResponse addMember(Long projectId, AddMemberRequest request, Long userId) {
        log.info("Creating invitation for project: {} by user: {}", projectId, userId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Only Product Owner can add Scrum Master, Scrum Master can add
        // Developers/Testers
        validateMemberManagementAccess(project, userId, request.getRole());

        User invitedUser = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        User invitedBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Inviter not found"));

        // Check if user has verified their email
        if (invitedUser.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("User must verify their email before being invited to projects");
        }

        // Check if user is already a member
        if (projectMemberRepository.findByProjectAndUser(project, invitedUser).isPresent()) {
            throw new DuplicateResourceException("User is already a member of this project");
        }

        if (projectInvitationRepository.existsByProjectAndUser(project, invitedUser)) {
            throw new DuplicateResourceException("User already has a pending invitation for this project");
        }

        // Create invitation
        String invitationToken = UUID.randomUUID().toString();
        ProjectInvitation invitation = ProjectInvitation.builder()
                .project(project)
                .user(invitedUser)
                .invitedBy(invitedBy)
                .role(request.getRole())
                .status(InvitationStatus.PENDING)
                .invitedAt(LocalDateTime.now())
                .invitationToken(invitationToken)
                .build();

        ProjectInvitation savedInvitation = projectInvitationRepository.save(invitation);

        try {
            notificationPublisher.publishProjectInvitation(
                    invitedUser.getId(),
                    project.getId(),
                    project.getName(),
                    invitedBy.getUsername(),
                    request.getRole().name());
            log.info("WebSocket notification sent for invitation");
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification for invitation", e);
        }

        // Send email invitation
        try {
            emailService.sendProjectInvitation(
                    invitedUser.getEmail(),
                    invitedUser.getUsername(),
                    project.getName(),
                    invitedBy.getUsername(),
                    request.getRole().name(),
                    invitationToken);
            log.info("Email invitation sent to: {}", invitedUser.getEmail());
        } catch (Exception e) {
            log.error("Failed to send email invitation", e);
        }

        log.info("Invitation created successfully for user: {} to project: {}", invitedUser.getUsername(),
                project.getName());

        return ProjectMemberResponse.builder()
                .id(savedInvitation.getId())
                .userId(invitedUser.getId())
                .username(invitedUser.getUsername())
                .email(invitedUser.getEmail())
                .role(request.getRole())
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "projectMembers", key = "#projectId")
    public void removeMember(Long projectId, Long memberId, Long userId) {
        log.info("Removing member: {} from project: {} by user: {}", memberId, projectId, userId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        ProjectMember memberToRemove = projectMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found"));

        if (!memberToRemove.getProject().getId().equals(projectId)) {
            throw new UnauthorizedException("Member does not belong to this project");
        }

        // Cannot remove Product Owner
        if (memberToRemove.getRole() == ProjectRole.PRODUCT_OWNER) {
            throw new UnauthorizedException("Cannot remove Product Owner from project");
        }

        // Validate access based on role being removed
        validateMemberManagementAccess(project, userId, memberToRemove.getRole());

        projectMemberRepository.delete(memberToRemove);

        log.info("Member removed successfully: {}", memberToRemove.getUser().getUsername());
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "projectMembers", key = "#projectId")
    public List<ProjectMemberResponse> getProjectMembers(Long projectId, Long userId) {
        log.info("Getting members for project: {} by user: {}", projectId, userId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        validateUserAccess(project, userId);

        List<ProjectMember> members = projectMemberRepository.findByProject(project);

        return members.stream()
                .map(this::buildMemberResponse)
                .collect(Collectors.toList());
    }

    // Helper methods

    private void validateUserAccess(Project project, Long userId) {
        projectMemberRepository.findByProjectAndUserId(project, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this project"));
    }

    private void validateProductOwnerAccess(Project project, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectAndUserId(project, userId)
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this project"));

        if (!authorizationService.isProductOwner(member)) {
            throw new UnauthorizedException("Only Product Owner can perform this action");
        }
    }

    private ProjectMember validateMemberManagementAccess(Project project, Long userId, ProjectRole roleToAdd) {
        ProjectMember currentMember = projectMemberRepository.findByProjectAndUserId(project, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this project"));

        // Product Owner cannot be added (only one per project at creation)
        if (roleToAdd == ProjectRole.PRODUCT_OWNER) {
            throw new UnauthorizedException("Cannot add Product Owner - already exists");
        }

        ValidationResult validationResult = authorizationService.canAddMemberWithRole(currentMember, roleToAdd);
        if (!validationResult.isValid()) {
            throw new UnauthorizedException(validationResult.getErrorMessage());
        }

        return currentMember;
    }

    private ProjectResponse buildProjectResponse(Project project, boolean includeMembers) {
        ProjectResponse.ProjectResponseBuilder builder = ProjectResponse.builder()
                .id(project.getId())
                .projectKey(project.getKey())
                .name(project.getName())
                .description(project.getDescription())
                .createdAt(project.getCreatedAt())
                .updatedAt(project.getUpdatedAt());

        List<ProjectMember> members = projectMemberRepository.findByProject(project);
        builder.membersCount(members.size());

        members.stream()
                .filter(m -> m.getRole() == ProjectRole.PRODUCT_OWNER)
                .findFirst()
                .ifPresent(po -> builder.productOwner(po.getUser().getFullName()));

        if (includeMembers) {
            builder.members(members.stream()
                    .map(this::buildMemberResponse)
                    .collect(Collectors.toList()));
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
