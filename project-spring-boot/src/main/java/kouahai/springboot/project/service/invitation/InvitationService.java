package kouahai.springboot.project.service.invitation;

import kouahai.springboot.project.dto.invitation.InvitationResponse;

import java.util.List;

public interface InvitationService {

    InvitationResponse acceptInvitation(String token, Long userId);

    InvitationResponse denyInvitation(String token, Long userId);

    List<InvitationResponse> getUserPendingInvitations(Long userId);

    List<InvitationResponse> getProjectInvitations(Long projectId, Long userId);
}
