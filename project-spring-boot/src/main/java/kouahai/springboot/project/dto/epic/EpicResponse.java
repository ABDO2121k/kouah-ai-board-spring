package kouahai.springboot.project.dto.epic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EpicResponse {

    private Long id;
    private String title;
    private String description;
    private Long productBacklogId;
    private Long projectId;
    private String projectName;
    private Integer userStoriesCount;
}
