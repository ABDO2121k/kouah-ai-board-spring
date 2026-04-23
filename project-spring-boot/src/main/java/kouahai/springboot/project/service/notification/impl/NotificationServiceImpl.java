package kouahai.springboot.project.service.notification.impl;

import kouahai.springboot.project.dto.notification.NotificationResponse;
import kouahai.springboot.project.exception.ResourceNotFoundException;
import kouahai.springboot.project.exception.UnauthorizedException;
import kouahai.springboot.project.model.*;
import kouahai.springboot.project.repository.NotificationRepository;
import kouahai.springboot.project.repository.ProjectMemberRepository;
import kouahai.springboot.project.repository.UserRepository;
import kouahai.springboot.project.service.notification.NotificationService;
import kouahai.springboot.project.service.notification.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final NotificationPublisher notificationPublisher;

    @Override
    @Transactional
    public void sendNotificationToUser(User user, String title, String message, NotificationType type,
            Long relatedEntityId, String relatedEntityType, Project project) {
        log.info("Sending notification to user: {} - {}", user.getUsername(), title);

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setRelatedEntityId(relatedEntityId);
        notification.setRelatedEntityType(relatedEntityType);
        notification.setProject(project);

        notificationRepository.save(notification);

        notificationPublisher.publishToUser(user, title, message, type, relatedEntityId, relatedEntityType, project);

        log.info("Notification saved successfully for user: {}", user.getUsername());
    }

    @Override
    @Transactional
    public void sendNotificationToProjectMembers(Project project, String title, String message,
            NotificationType type, Long relatedEntityId,
            String relatedEntityType, Long excludeUserId) {
        log.info("Sending notification to all members of project: {}", project.getName());

        List<ProjectMember> members = projectMemberRepository.findByProject(project);

        members.stream()
                .filter(member -> !member.getUser().getId().equals(excludeUserId))
                .forEach(member -> sendNotificationToUser(
                        member.getUser(),
                        title,
                        message,
                        type,
                        relatedEntityId,
                        relatedEntityType,
                        project));
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUserNotifications(Long userId) {
        log.info("Getting all notifications for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Notification> notifications = notificationRepository.findByUserOrderByCreatedAtDesc(user);

        return notifications.stream()
                .map(this::buildNotificationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        log.info("Getting unread notifications for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Notification> notifications = notificationRepository.findByUserAndIsReadOrderByCreatedAtDesc(user, false);

        return notifications.stream()
                .map(this::buildNotificationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return notificationRepository.countByUserAndIsRead(user, false);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId, Long userId) {
        log.info("Marking notification: {} as read by user: {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You cannot mark this notification as read");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        log.info("Marking all notifications as read for user: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        notificationRepository.markAllAsReadByUser(user);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId, Long userId) {
        log.info("Deleting notification: {} by user: {}", notificationId, userId);

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You cannot delete this notification");
        }

        notificationRepository.delete(notification);
    }

    private NotificationResponse buildNotificationResponse(Notification notification) {
        NotificationResponse.NotificationResponseBuilder builder = NotificationResponse.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .relatedEntityId(notification.getRelatedEntityId())
                .relatedEntityType(notification.getRelatedEntityType());

        if (notification.getProject() != null) {
            builder.projectId(notification.getProject().getId())
                    .projectName(notification.getProject().getName());
        }

        return builder.build();
    }
}
