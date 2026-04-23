package kouahai.springboot.project.service.notification;

import kouahai.springboot.project.dto.notification.NotificationResponse;
import kouahai.springboot.project.model.NotificationType;
import kouahai.springboot.project.model.Project;
import kouahai.springboot.project.model.User;

import java.util.List;

/**
 * Service for managing notifications in the database
 */
public interface NotificationService {

    /**
     * Send notification to a specific user
     */
    void sendNotificationToUser(User user, String title, String message, NotificationType type,
            Long relatedEntityId, String relatedEntityType, Project project);

    /**
     * Send notification to all members of a project (excluding a specific user)
     */
    void sendNotificationToProjectMembers(Project project, String title, String message,
            NotificationType type, Long relatedEntityId, String relatedEntityType, Long excludeUserId);

    /**
     * Get all notifications for a user
     */
    List<NotificationResponse> getUserNotifications(Long userId);

    /**
     * Get unread notifications for a user
     */
    List<NotificationResponse> getUnreadNotifications(Long userId);

    /**
     * Get count of unread notifications for a user
     */
    Long getUnreadCount(Long userId);

    /**
     * Mark a notification as read
     */
    void markAsRead(Long notificationId, Long userId);

    /**
     * Mark all notifications as read for a user
     */
    void markAllAsRead(Long userId);

    /**
     * Delete a notification
     */
    void deleteNotification(Long notificationId, Long userId);
}
