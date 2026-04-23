package kouahai.springboot.project.dto.userstory;

import kouahai.springboot.project.model.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStoryResponse {

    private Long id;
    private String title;
    private String asRole;
    private String iWant;
    private String soThat;
    private Integer storyPoints;
    private Priority priority;
    private Long epicId;
    private String epicTitle;
    private Long sprintId;
    private String sprintName;
    private Long productBacklogId;
    private Integer acceptanceScenariosCount;
    private List<AcceptanceScenarioResponse> acceptanceScenarios;
}
