package kouahai.springboot.project.dto.project;

import com.fasterxml.jackson.annotation.JsonInclude;

import kouahai.springboot.project.model.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProjectMemberResponse {

    private Long id;
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private ProjectRole role;
}
