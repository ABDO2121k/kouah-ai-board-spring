package kouahai.springboot.project.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProjectInfo implements Serializable {
    private Long userId;
    private Long projectId;
    private String projectName;
    private String projectKey;
    private String role;
}
