package kouahai.springboot.project.service.notification;

import kouahai.springboot.project.dto.notification.UserProjectInfo;
import kouahai.springboot.project.model.NotificationType;
import kouahai.springboot.project.model.Project;
import kouahai.springboot.project.model.User;

import java.util.List;

/**
 * Service for publishing notifications to RabbitMQ
 */
public interface NotificationPublisher {

        /**
         * Publish notification to specific user
         */
        void publishToUser(User user, String title, String message, NotificationType type,
                        Long relatedEntityId, String relatedEntityType, Project project);

        /**
         * Publish notification to all members of a project
         */
        void publishToProject(Project project, String title, String message, NotificationType type,
                        Long relatedEntityId, String relatedEntityType);

        /**
         * Publish notification to users with specific role in project
         */
        void publishToProjectRole(Project project, String role, String title, String message,
                        NotificationType type, Long relatedEntityId, String relatedEntityType);

        /**
         * Publish notification to specific user in specific project
         */
        void publishToProjectUser(Project project, User user, String title, String message,
                        NotificationType type, Long relatedEntityId, String relatedEntityType);

        /**
         * Broadcast notification to all connected users
         */
        void publishBroadcast(String title, String message, NotificationType type);

        /**
         * Publish project invitation notification
         */
        void publishProjectInvitation(Long userId, Long projectId, String projectName,
                        String invitedBy, String role);

        /**
         * Publish invitation response notification (accepted/denied)
         */
        void publishInvitationResponse(Long userId, Long projectId, String projectName,
                        String respondedBy, String action);

        /**
         * Get user's projects for WebSocket channel subscription
         */
        List<UserProjectInfo> getUserProjects(Long userId);
}
