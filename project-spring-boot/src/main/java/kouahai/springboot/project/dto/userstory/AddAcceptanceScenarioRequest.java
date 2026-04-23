package kouahai.springboot.project.dto.userstory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddAcceptanceScenarioRequest {

    @NotBlank(message = "Given condition is required")
    @Size(max = 1000, message = "Given condition must not exceed 1000 characters")
    private String givenCondition;

    @NotBlank(message = "When action is required")
    @Size(max = 1000, message = "When action must not exceed 1000 characters")
    private String whenAction;

    @NotBlank(message = "Then result is required")
    @Size(max = 1000, message = "Then result must not exceed 1000 characters")
    private String thenResult;
}
