package kouahai.springboot.project.service.userstory;

import java.util.List;

import kouahai.springboot.project.dto.userstory.AddAcceptanceScenarioRequest;
import kouahai.springboot.project.dto.userstory.CreateUserStoryRequest;
import kouahai.springboot.project.dto.userstory.UserStoryResponse;
import kouahai.springboot.project.dto.userstory.AcceptanceScenarioResponse;

public interface UserStoryService {

    UserStoryResponse createUserStory(Long projectId, CreateUserStoryRequest request, Long userId);

    UserStoryResponse getUserStoryById(Long userStoryId, Long userId);

    List<UserStoryResponse> getProjectUserStories(Long projectId, Long userId);

    List<UserStoryResponse> getEpicUserStories(Long epicId, Long userId);

    UserStoryResponse updateUserStory(Long userStoryId, CreateUserStoryRequest request, Long userId);

    void deleteUserStory(Long userStoryId, Long userId);

    UserStoryResponse estimateStoryPoints(Long userStoryId, Integer storyPoints, Long userId);

    AcceptanceScenarioResponse addAcceptanceScenario(Long userStoryId, AddAcceptanceScenarioRequest request,
            Long userId);

    void deleteAcceptanceScenario(Long scenarioId, Long userId);

    List<AcceptanceScenarioResponse> getAcceptanceScenarios(Long userStoryId, Long userId);
}
