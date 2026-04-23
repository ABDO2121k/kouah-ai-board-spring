package kouahai.springboot.project.service.notification.impl;

import kouahai.springboot.project.config.RabbitMQConfig;
import kouahai.springboot.project.dto.notification.NotificationMessage;
import kouahai.springboot.project.dto.notification.UserProjectInfo;
import kouahai.springboot.project.model.NotificationType;
import kouahai.springboot.project.model.Project;
import kouahai.springboot.project.model.ProjectMember;
import kouahai.springboot.project.model.User;
import kouahai.springboot.project.repository.ProjectMemberRepository;
import kouahai.springboot.project.service.notification.NotificationPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationPublisherImpl implements NotificationPublisher {

        private final RabbitTemplate rabbitTemplate;
        private final ProjectMemberRepository projectMemberRepository;

        @Override
        public void publishToUser(User user, String title, String message, NotificationType type,
                        Long relatedEntityId, String relatedEntityType, Project project) {
                NotificationMessage.NotificationData data = buildNotificationData(
                                null, title, message, type, relatedEntityId, relatedEntityType, project, false);

                NotificationMessage notificationMessage = NotificationMessage.builder()
                                .targetType("USER")
                                .targetId(user.getId())
                                .data(data)
                                .build();

                publishNotification("notification.user", notificationMessage);
                log.info("Published notification to user {}: {}", user.getUsername(), title);
        }

        @Override
        public void publishToProject(Project project, String title, String message, NotificationType type,
                        Long relatedEntityId, String relatedEntityType) {
                NotificationMessage.NotificationData data = buildNotificationData(
                                null, title, message, type, relatedEntityId, relatedEntityType, project, false);

                NotificationMessage notificationMessage = NotificationMessage.builder()
                                .targetType("PROJECT")
                                .projectId(project.getId())
                                .data(data)
                                .build();

                publishNotification("notification.project", notificationMessage);
                log.info("Published notification to project {}: {}", project.getName(), title);
        }

        @Override
        public void publishToProjectRole(Project project, String role, String title, String message,
                        NotificationType type, Long relatedEntityId, String relatedEntityType) {
                NotificationMessage.NotificationData data = buildNotificationData(
                                null, title, message, type, relatedEntityId, relatedEntityType, project, false);

                NotificationMessage notificationMessage = NotificationMessage.builder()
                                .targetType("PROJECT_ROLE")
                                .projectId(project.getId())
                                .role(role)
                                .data(data)
                                .build();

                publishNotification("notification.project.role", notificationMessage);
                log.info("Published notification to project {} role {}: {}", project.getName(), role, title);
        }

        @Override
        public void publishToProjectUser(Project project, User user, String title, String message,
                        NotificationType type, Long relatedEntityId, String relatedEntityType) {
                NotificationMessage.NotificationData data = buildNotificationData(
                                null, title, message, type, relatedEntityId, relatedEntityType, project, false);

                NotificationMessage notificationMessage = NotificationMessage.builder()
                                .targetType("PROJECT_USER")
                                .projectId(project.getId())
                                .userId(user.getId())
                                .data(data)
                                .build();

                publishNotification("notification.project.user", notificationMessage);
                log.info("Published notification to user {} in project {}: {}",
                                user.getUsername(), project.getName(), title);
        }

        @Override
        public void publishBroadcast(String title, String message, NotificationType type) {
                NotificationMessage.NotificationData data = buildNotificationData(
                                null, title, message, type, null, null, null, false);

                NotificationMessage notificationMessage = NotificationMessage.builder()
                                .targetType("BROADCAST")
                                .data(data)
                                .build();

                publishNotification("notification.broadcast", notificationMessage);
                log.info("Published broadcast notification: {}", title);
        }

        @Override
        public void publishProjectInvitation(Long userId, Long projectId, String projectName,
                        String invitedBy, String role) {
                NotificationMessage.NotificationData data = NotificationMessage.NotificationData.builder()
                                .title("Project Invitation")
                                .message(String.format("You have been invited to join project '%s' as %s by %s",
                                                projectName, role, invitedBy))
                                .type(NotificationType.PROJECT_INVITATION.name())
                                .relatedEntityId(projectId)
                                .relatedEntityType("PROJECT")
                                .projectId(projectId)
                                .projectName(projectName)
                                .timestamp(LocalDateTime.now())
                                .read(false)
                                .build();

                NotificationMessage notificationMessage = NotificationMessage.builder()
                                .targetType("USER")
                                .targetId(userId)
                                .data(data)
                                .build();

                publishNotification("notification.invitation", notificationMessage);
                log.info("Published invitation notification to user {}: project {}", userId, projectName);
        }

        @Override
        public void publishInvitationResponse(Long userId, Long projectId, String projectName,
                        String respondedBy, String action) {
                NotificationMessage.NotificationData data = NotificationMessage.NotificationData.builder()
                                .title("Invitation " + action)
                                .message(String.format("%s has %s the invitation to join project '%s'",
                                                respondedBy, action.toLowerCase(), projectName))
                                .type(NotificationType.PROJECT_UPDATED.name())
                                .relatedEntityId(projectId)
                                .relatedEntityType("PROJECT")
                                .projectId(projectId)
                                .projectName(projectName)
                                .timestamp(LocalDateTime.now())
                                .read(false)
                                .build();

                NotificationMessage notificationMessage = NotificationMessage.builder()
                                .targetType("USER")
                                .targetId(userId)
                                .data(data)
                                .build();

                publishNotification("notification.invitation.response", notificationMessage);
                log.info("Published invitation {} notification to user {}", action, userId);
        }

        @Override
        @RabbitListener(queues = RabbitMQConfig.USER_PROJECTS_REQUEST_QUEUE)
        @SendTo
        public List<UserProjectInfo> getUserProjects(Long userId) {
                log.info("Received request for user {} projects", userId);

                List<ProjectMember> projectMembers = projectMemberRepository.findByUserId(userId);

                return projectMembers.stream()
                                .map(pm -> UserProjectInfo.builder()
                                                .userId(userId)
                                                .projectId(pm.getProject().getId())
                                                .projectName(pm.getProject().getName())
                                                .projectKey(pm.getProject().getProjectKey())
                                                .role(pm.getRole().name())
                                                .build())
                                .collect(Collectors.toList());
        }

        private void publishNotification(String routingKey, NotificationMessage message) {
                try {
                        rabbitTemplate.convertAndSend(
                                        RabbitMQConfig.NOTIFICATION_EXCHANGE,
                                        routingKey,
                                        message);
                } catch (Exception e) {
                        log.error("Failed to publish notification to RabbitMQ: {}", e.getMessage(), e);
                }
        }

        private NotificationMessage.NotificationData buildNotificationData(
                        Long id, String title, String message, NotificationType type,
                        Long relatedEntityId, String relatedEntityType, Project project, Boolean read) {

                return NotificationMessage.NotificationData.builder()
                                .id(id)
                                .title(title)
                                .message(message)
                                .type(type != null ? type.name() : null)
                                .relatedEntityId(relatedEntityId)
                                .relatedEntityType(relatedEntityType)
                                .projectId(project != null ? project.getId() : null)
                                .projectName(project != null ? project.getName() : null)
                                .timestamp(LocalDateTime.now())
                                .read(read)
                                .build();
        }
}
