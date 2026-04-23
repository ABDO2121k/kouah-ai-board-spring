package kouahai.springboot.project.service.invitation.impl;

import kouahai.springboot.project.dto.invitation.InvitationResponse;
import kouahai.springboot.project.exception.ResourceNotFoundException;
import kouahai.springboot.project.exception.UnauthorizedException;
import kouahai.springboot.project.model.*;
import kouahai.springboot.project.repository.ProjectInvitationRepository;
import kouahai.springboot.project.repository.ProjectMemberRepository;
import kouahai.springboot.project.repository.ProjectRepository;
import kouahai.springboot.project.repository.UserRepository;
import kouahai.springboot.project.service.invitation.InvitationService;
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
public class InvitationServiceImpl implements InvitationService {

    private final ProjectInvitationRepository invitationRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final NotificationPublisher notificationPublisher;

    @Override
    @Transactional
    public InvitationResponse acceptInvitation(String token, Long userId) {
        log.info("User {} accepting invitation with token", userId);

        ProjectInvitation invitation = invitationRepository.findByInvitationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found or expired"));

        // Validate user is the invited user
        if (!invitation.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to accept this invitation");
        }

        // Validate invitation is still pending
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new UnauthorizedException(
                    "Invitation has already been " + invitation.getStatus().name().toLowerCase());
        }

        // Create project member
        ProjectMember newMember = new ProjectMember();
        newMember.setProject(invitation.getProject());
        newMember.setUser(invitation.getUser());
        newMember.setRole(invitation.getRole());
        newMember.setJoinedAt(LocalDateTime.now());
        projectMemberRepository.save(newMember);

        // Update invitation status
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitation.setRespondedAt(LocalDateTime.now());
        ProjectInvitation updatedInvitation = invitationRepository.save(invitation);

        // Send notification to inviter
        try {
            notificationPublisher.publishInvitationResponse(
                    invitation.getInvitedBy().getId(),
                    invitation.getProject().getId(),
                    invitation.getProject().getName(),
                    invitation.getUser().getUsername(),
                    "ACCEPTED");
        } catch (Exception e) {
            log.error("Failed to send invitation acceptance notification", e);
        }

        log.info("Invitation accepted: User {} joined project {}", userId, invitation.getProject().getName());

        return buildInvitationResponse(updatedInvitation);
    }

    @Override
    @Transactional
    public InvitationResponse denyInvitation(String token, Long userId) {
        log.info("User {} denying invitation with token", userId);

        ProjectInvitation invitation = invitationRepository.findByInvitationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invitation not found or expired"));

        // Validate user is the invited user
        if (!invitation.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You are not authorized to deny this invitation");
        }

        // Validate invitation is still pending
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new UnauthorizedException(
                    "Invitation has already been " + invitation.getStatus().name().toLowerCase());
        }

        // Update invitation status
        invitation.setStatus(InvitationStatus.DENIED);
        invitation.setRespondedAt(LocalDateTime.now());
        ProjectInvitation updatedInvitation = invitationRepository.save(invitation);

        // Send notification to inviter
        try {
            notificationPublisher.publishInvitationResponse(
                    invitation.getInvitedBy().getId(),
                    invitation.getProject().getId(),
                    invitation.getProject().getName(),
                    invitation.getUser().getUsername(),
                    "DENIED");
        } catch (Exception e) {
            log.error("Failed to send invitation denial notification", e);
        }

        // Remove the invitation
        invitationRepository.delete(invitation);

        log.info("Invitation denied and removed: User {} declined project {}", userId,
                invitation.getProject().getName());

        return buildInvitationResponse(updatedInvitation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvitationResponse> getUserPendingInvitations(Long userId) {
        log.info("Getting pending invitations for user {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<ProjectInvitation> invitations = invitationRepository.findByUserAndStatus(user, InvitationStatus.PENDING);

        return invitations.stream()
                .map(this::buildInvitationResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvitationResponse> getProjectInvitations(Long projectId, Long userId) {
        log.info("Getting invitations for project {} by user {}", projectId, userId);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // Validate user has access to project
        ProjectMember member = projectMemberRepository.findByProjectAndUserId(project, userId)
                .orElseThrow(() -> new UnauthorizedException("You don't have access to this project"));

        // Only Product Owner and Scrum Master can view invitations
        if (member.getRole() != ProjectRole.PRODUCT_OWNER && member.getRole() != ProjectRole.SCRUM_MASTER) {
            throw new UnauthorizedException("Only Product Owner and Scrum Master can view project invitations");
        }

        List<ProjectInvitation> invitations = invitationRepository.findByProjectAndStatus(project,
                InvitationStatus.PENDING);

        return invitations.stream()
                .map(this::buildInvitationResponse)
                .collect(Collectors.toList());
    }

    private InvitationResponse buildInvitationResponse(ProjectInvitation invitation) {
        return InvitationResponse.builder()
                .id(invitation.getId())
                .projectId(invitation.getProject().getId())
                .projectName(invitation.getProject().getName())
                .projectKey(invitation.getProject().getKey())
                .userId(invitation.getUser().getId())
                .userEmail(invitation.getUser().getEmail())
                .userName(invitation.getUser().getUsername())
                .invitedById(invitation.getInvitedBy().getId())
                .invitedByName(invitation.getInvitedBy().getUsername())
                .role(invitation.getRole())
                .status(invitation.getStatus())
                .invitedAt(invitation.getInvitedAt())
                .respondedAt(invitation.getRespondedAt())
                .build();
    }
}
