package kouahai.springboot.project.service.email;

import kouahai.springboot.project.model.User;

public interface EmailService {

    void createVerificationToken(User user);

    boolean verifyEmail(String token);

    void createPasswordResetToken(User user);

    void cleanupExpiredTokens();

    void sendProjectInvitation(String emailAddress, String username, String projectName,
            String invitedBy, String role, String invitationToken);
}
