package kouahai.springboot.project.dto.sprint;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import kouahai.springboot.project.dto.project.ProjectMemberResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SprintResponse {

    private Long id;
    private String name;
    private String sprintGoal;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private boolean isActive;
    private Integer membersCount;
    private List<ProjectMemberResponse> members;
}
