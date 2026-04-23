package kouahai.springboot.project.service.userstory.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kouahai.springboot.project.dto.userstory.AcceptanceScenarioResponse;
import kouahai.springboot.project.dto.userstory.AddAcceptanceScenarioRequest;
import kouahai.springboot.project.dto.userstory.CreateUserStoryRequest;
import kouahai.springboot.project.dto.userstory.UserStoryResponse;
import kouahai.springboot.project.exception.ResourceNotFoundException;
import kouahai.springboot.project.exception.UnauthorizedException;
import kouahai.springboot.project.model.AcceptanceScenario;
import kouahai.springboot.project.model.Epic;
import kouahai.springboot.project.model.ProductBacklog;
import kouahai.springboot.project.model.Project;
import kouahai.springboot.project.model.ProjectMember;
import kouahai.springboot.project.model.ProjectRole;
import kouahai.springboot.project.model.UserStory;
import kouahai.springboot.project.repository.AcceptanceScenarioRepository;
import kouahai.springboot.project.repository.EpicRepository;
import kouahai.springboot.project.repository.ProductBacklogRepository;
import kouahai.springboot.project.repository.ProjectMemberRepository;
import kouahai.springboot.project.repository.ProjectRepository;
import kouahai.springboot.project.repository.UserStoryRepository;
import kouahai.springboot.project.service.userstory.UserStoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserStoryServiceImpl implements UserStoryService {

    private final UserStoryRepository userStoryRepository;
    private final AcceptanceScenarioRepository acceptanceScenarioRepository;
    private final ProjectRepository projectRepository;
    private final ProductBacklogRepository productBacklogRepository;
    private final EpicRepository epicRepository;
    private final ProjectMemberRepository projectMemberRepository;

    @Override
    @Transactional
    public UserStoryResponse createUserStory(Long projectId, CreateUserStoryRequest request, Long userId) {
        log.info("Creating user story for project: {} by user: {}", projectId, userId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Product Owner or Scrum Master can create user stories
        validateProductOwnerOrScrumMasterAccess(project, userId);

        ProductBacklog backlog = productBacklogRepository.findByProject(project)
                .orElseThrow(() -> new ResourceNotFoundException("Product backlog not found for project"));

        UserStory userStory = new UserStory();
        userStory.setTitle(request.getTitle());
        userStory.setAsRole(request.getAsRole());
        userStory.setIWant(request.getIWant());
        userStory.setSoThat(request.getSoThat());
        userStory.setPriority(request.getPriority());
        userStory.setProductBacklog(backlog);

        // Set epic if provided
        if (request.getEpicId() != null) {
            Epic epic = epicRepository.findById(request.getEpicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Epic not found"));

            // Verify epic belongs to the same project
            if (!epic.getProductBacklog().getProject().getId().equals(projectId)) {
                throw new UnauthorizedException("Epic does not belong to this project");
            }

            userStory.setEpic(epic);
        }

        UserStory savedUserStory = userStoryRepository.save(userStory);

        log.info("User story created successfully: {}", savedUserStory.getTitle());

        return buildUserStoryResponse(savedUserStory, false);
    }

    @Override
    @Transactional(readOnly = true)
    public UserStoryResponse getUserStoryById(Long userStoryId, Long userId) {
        log.info("Getting user story by ID: {} for user: {}", userStoryId, userId);

        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new ResourceNotFoundException("User story not found"));

        Project project = userStory.getProductBacklog().getProject();
        validateUserAccess(project, userId);

        return buildUserStoryResponse(userStory, true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStoryResponse> getProjectUserStories(Long projectId, Long userId) {
        log.info("Getting all user stories for project: {} by user: {}", projectId, userId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        validateUserAccess(project, userId);

        ProductBacklog backlog = productBacklogRepository.findByProject(project)
                .orElseThrow(() -> new ResourceNotFoundException("Product backlog not found for project"));

        List<UserStory> userStories = userStoryRepository.findByProductBacklog(backlog);

        return userStories.stream()
                .map(us -> buildUserStoryResponse(us, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserStoryResponse> getEpicUserStories(Long epicId, Long userId) {
        log.info("Getting user stories for epic: {} by user: {}", epicId, userId);

        Epic epic = epicRepository.findById(epicId)
                .orElseThrow(() -> new ResourceNotFoundException("Epic not found"));

        Project project = epic.getProductBacklog().getProject();
        validateUserAccess(project, userId);

        List<UserStory> userStories = userStoryRepository.findByEpic(epic);

        return userStories.stream()
                .map(us -> buildUserStoryResponse(us, false))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserStoryResponse updateUserStory(Long userStoryId, CreateUserStoryRequest request, Long userId) {
        log.info("Updating user story: {} by user: {}", userStoryId, userId);

        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new ResourceNotFoundException("User story not found"));

        Project project = userStory.getProductBacklog().getProject();
        validateProductOwnerOrScrumMasterAccess(project, userId);

        userStory.setTitle(request.getTitle());
        userStory.setAsRole(request.getAsRole());
        userStory.setIWant(request.getIWant());
        userStory.setSoThat(request.getSoThat());
        userStory.setPriority(request.getPriority());

        // Update epic if provided
        if (request.getEpicId() != null) {
            Epic epic = epicRepository.findById(request.getEpicId())
                    .orElseThrow(() -> new ResourceNotFoundException("Epic not found"));

            if (!epic.getProductBacklog().getProject().getId().equals(project.getId())) {
                throw new UnauthorizedException("Epic does not belong to this project");
            }

            userStory.setEpic(epic);
        } else {
            userStory.setEpic(null);
        }

        UserStory updatedUserStory = userStoryRepository.save(userStory);

        log.info("User story updated successfully: {}", updatedUserStory.getTitle());

        return buildUserStoryResponse(updatedUserStory, true);
    }

    @Override
    @Transactional
    public void deleteUserStory(Long userStoryId, Long userId) {
        log.info("Deleting user story: {} by user: {}", userStoryId, userId);

        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new ResourceNotFoundException("User story not found"));

        Project project = userStory.getProductBacklog().getProject();
        validateProductOwnerOrScrumMasterAccess(project, userId);

        userStoryRepository.delete(userStory);

        log.info("User story deleted successfully");
    }

    @Override
    @Transactional
    public UserStoryResponse estimateStoryPoints(Long userStoryId, Integer storyPoints, Long userId) {
        log.info("Estimating story points for user story: {} with points: {}", userStoryId, storyPoints);

        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new ResourceNotFoundException("User story not found"));

        Project project = userStory.getProductBacklog().getProject();
        // Only Scrum Master can estimate story points
        validateScrumMasterAccess(project, userId);

        userStory.setStoryPoints(storyPoints);
        UserStory updatedUserStory = userStoryRepository.save(userStory);

        log.info("Story points estimated successfully");

        return buildUserStoryResponse(updatedUserStory, false);
    }

    @Override
    @Transactional
    public AcceptanceScenarioResponse addAcceptanceScenario(Long userStoryId, AddAcceptanceScenarioRequest request,
            Long userId) {
        log.info("Adding acceptance scenario to user story: {}", userStoryId);

        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new ResourceNotFoundException("User story not found"));

        Project project = userStory.getProductBacklog().getProject();
        // Only Scrum Master can add acceptance scenarios
        validateScrumMasterAccess(project, userId);

        AcceptanceScenario scenario = new AcceptanceScenario();
        scenario.setGivenCondition(request.getGivenCondition());
        scenario.setWhenAction(request.getWhenAction());
        scenario.setThenResult(request.getThenResult());
        scenario.setUserStory(userStory);

        AcceptanceScenario savedScenario = acceptanceScenarioRepository.save(scenario);

        log.info("Acceptance scenario added successfully");

        return buildAcceptanceScenarioResponse(savedScenario);
    }

    @Override
    @Transactional
    public void deleteAcceptanceScenario(Long scenarioId, Long userId) {
        log.info("Deleting acceptance scenario: {}", scenarioId);

        AcceptanceScenario scenario = acceptanceScenarioRepository.findById(scenarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Acceptance scenario not found"));

        Project project = scenario.getUserStory().getProductBacklog().getProject();
        validateScrumMasterAccess(project, userId);

        acceptanceScenarioRepository.delete(scenario);

        log.info("Acceptance scenario deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<AcceptanceScenarioResponse> getAcceptanceScenarios(Long userStoryId, Long userId) {
        log.info("Getting acceptance scenarios for user story: {}", userStoryId);

        UserStory userStory = userStoryRepository.findById(userStoryId)
                .orElseThrow(() -> new ResourceNotFoundException("User story not found"));

        Project project = userStory.getProductBacklog().getProject();
        validateUserAccess(project, userId);

        List<AcceptanceScenario> scenarios = acceptanceScenarioRepository.findByUserStory(userStory);

        return scenarios.stream()
                .map(this::buildAcceptanceScenarioResponse)
                .collect(Collectors.toList());
    }

    // Helper methods

    private void validateUserAccess(Project project, Long userId) {
        projectMemberRepository.findByProjectAndUserId(project, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this project"));
    }

    private void validateProductOwnerOrScrumMasterAccess(Project project, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectAndUserId(project, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this project"));

        if (member.getRole() != ProjectRole.PRODUCT_OWNER && member.getRole() != ProjectRole.SCRUM_MASTER) {
            throw new UnauthorizedException("Only Product Owner or Scrum Master can perform this action");
        }
    }

    private void validateScrumMasterAccess(Project project, Long userId) {
        ProjectMember member = projectMemberRepository.findByProjectAndUserId(project, userId)
                .orElseThrow(() -> new UnauthorizedException("You are not a member of this project"));

        if (member.getRole() != ProjectRole.SCRUM_MASTER) {
            throw new UnauthorizedException("Only Scrum Master can perform this action");
        }
    }

    private UserStoryResponse buildUserStoryResponse(UserStory userStory, boolean includeScenarios) {
        UserStoryResponse.UserStoryResponseBuilder builder = UserStoryResponse.builder()
                .id(userStory.getId())
                .title(userStory.getTitle())
                .asRole(userStory.getAsRole())
                .iWant(userStory.getIWant())
                .soThat(userStory.getSoThat())
                .storyPoints(userStory.getStoryPoints())
                .priority(userStory.getPriority())
                .productBacklogId(userStory.getProductBacklog().getId());

        if (userStory.getEpic() != null) {
            builder.epicId(userStory.getEpic().getId())
                    .epicTitle(userStory.getEpic().getTitle());
        }

        if (userStory.getSprint() != null) {
            builder.sprintId(userStory.getSprint().getId())
                    .sprintName(userStory.getSprint().getName());
        }

        if (includeScenarios && userStory.getAcceptanceScenarios() != null) {
            builder.acceptanceScenarios(userStory.getAcceptanceScenarios().stream()
                    .map(this::buildAcceptanceScenarioResponse)
                    .collect(Collectors.toList()));
        }

        builder.acceptanceScenariosCount(
                userStory.getAcceptanceScenarios() != null ? userStory.getAcceptanceScenarios().size() : 0);

        return builder.build();
    }

    private AcceptanceScenarioResponse buildAcceptanceScenarioResponse(AcceptanceScenario scenario) {
        return AcceptanceScenarioResponse.builder()
                .id(scenario.getId())
                .givenCondition(scenario.getGivenCondition())
                .whenAction(scenario.getWhenAction())
                .thenResult(scenario.getThenResult())
                .build();
    }
}
