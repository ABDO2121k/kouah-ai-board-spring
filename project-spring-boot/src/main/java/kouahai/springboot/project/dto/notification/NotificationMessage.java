package kouahai.springboot.project.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {

    private String targetType; // USER, PROJECT, PROJECT_ROLE, PROJECT_USER, BROADCAST
    private Long targetId; // User ID or Project ID
    private Long userId; // Specific user ID (for PROJECT_USER)
    private Long projectId; // Project ID (for project-related notifications)
    private String role; // Role name (for PROJECT_ROLE)

    private NotificationData data;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class NotificationData implements Serializable {
        private Long id;
        private String title;
        private String message;
        private String type;
        private Long relatedEntityId;
        private String relatedEntityType;
        private Long projectId;
        private String projectName;
        private LocalDateTime timestamp;
        private Boolean read;
    }
}
