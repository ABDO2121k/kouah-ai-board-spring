package kouahai.springboot.project.dto.userstory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AcceptanceScenarioResponse {

    private Long id;
    private String givenCondition;
    private String whenAction;
    private String thenResult;
}
