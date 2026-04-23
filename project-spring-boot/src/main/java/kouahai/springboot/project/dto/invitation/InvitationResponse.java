package kouahai.springboot.project.dto.invitation;

import kouahai.springboot.project.model.InvitationStatus;
import kouahai.springboot.project.model.ProjectRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvitationResponse {
    private Long id;
    private Long projectId;
    private String projectName;
    private String projectKey;
    private Long userId;
    private String userEmail;
    private String userName;
    private Long invitedById;
    private String invitedByName;
    private ProjectRole role;
    private InvitationStatus status;
    private LocalDateTime invitedAt;
    private LocalDateTime respondedAt;
}
