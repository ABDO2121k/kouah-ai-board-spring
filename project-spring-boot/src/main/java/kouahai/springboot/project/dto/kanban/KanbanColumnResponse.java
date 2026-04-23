package kouahai.springboot.project.dto.kanban;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KanbanColumnResponse {

    private Long id;
    private String name;
    private Integer positionIndex;
    private boolean isDoneStatus;
    private boolean testerOnly;
    private String color;
    private Long sprintId;
    private String sprintName;
    private Integer tasksCount;
}
