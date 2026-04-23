package kouahai.springboot.project.service.email.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import email.Email.EmailResponse;
import email.Email.VerificationEmailRequest;
import email.EmailServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import kouahai.springboot.project.model.TokenType;
import kouahai.springboot.project.model.User;
import kouahai.springboot.project.model.UserStatus;
import kouahai.springboot.project.model.VerificationToken;
import kouahai.springboot.project.repository.UserRepository;
import kouahai.springboot.project.repository.VerificationTokenRepository;
import kouahai.springboot.project.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final ManagedChannel emailServiceChannel;

    @Value("${app.email.verification.url}")
    private String verificationUrl;

    @Override
    @Transactional
    public void createVerificationToken(User user) {
        log.info("Creating verification token for user: {}", user.getEmail());

        String token = UUID.randomUUID().toString();

        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setTokenType(TokenType.EMAIL_VERIFICATION);
        verificationToken.setUser(user);
        verificationToken.setExpiresAt(LocalDateTime.now().plusHours(24));

        tokenRepository.save(verificationToken);
        sendVerificationEmail(user, token);

        log.info("Verification email sent to: {}", user.getEmail());
    }

    @Override
    @Transactional
    public boolean verifyEmail(String token) {
        log.info("Verifying email with token");

        VerificationToken verificationToken = tokenRepository
                .findByTokenAndTokenType(token, TokenType.EMAIL_VERIFICATION)
                .orElse(null);

        if (verificationToken == null) {
            log.warn("Verification token not found");
            return false;
        }

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Verification token expired");
            return false;
        }

        User user = verificationToken.getUser();
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        tokenRepository.delete(verificationToken);

        log.info("Email verified successfully for user: {}", user.getEmail());
        return true;
    }

    @Override
    @Transactional
    public void createPasswordResetToken(User user) {
        log.info("Creating password reset token for user: {}", user.getEmail());

        String token = UUID.randomUUID().toString();

        VerificationToken resetToken = new VerificationToken();
        resetToken.setToken(token);
        resetToken.setTokenType(TokenType.PASSWORD_RESET);
        resetToken.setUser(user);
        resetToken.setExpiresAt(LocalDateTime.now().plusHours(2));

        tokenRepository.save(resetToken);
        sendPasswordResetEmail(user, token);

        log.info("Password reset email sent to: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Cleaning up expired tokens");
        tokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    @Override
    public void sendProjectInvitation(String emailAddress, String username, String projectName,
            String invitedBy, String role, String invitationToken) {
        log.info("Sending project invitation email to: {}", emailAddress);

        try {
            String invitationLink = verificationUrl.replace("/verify", "/invitation") + "?token=" + invitationToken;

            // Create gRPC stub
            EmailServiceGrpc.EmailServiceBlockingStub stub = EmailServiceGrpc.newBlockingStub(emailServiceChannel);

            // Build request using the ReportEmail as a workaround (or we could create a new
            // proto message)
            email.Email.ReportEmailRequest request = email.Email.ReportEmailRequest.newBuilder()
                    .setTo(emailAddress)
                    .setUsername(username)
                    .setReportTitle("Project Invitation")
                    .setReportContent("You have been invited to join the project '" + projectName +
                            "' by " + invitedBy + " as " + role + ".")
                    .setReportDate(LocalDateTime.now().toString())
                    .addReportData(email.Email.ReportData.newBuilder()
                            .setKey("projectName")
                            .setValue(projectName)
                            .build())
                    .addReportData(email.Email.ReportData.newBuilder()
                            .setKey("invitedBy")
                            .setValue(invitedBy)
                            .build())
                    .addReportData(email.Email.ReportData.newBuilder()
                            .setKey("role")
                            .setValue(role)
                            .build())
                    .addReportData(email.Email.ReportData.newBuilder()
                            .setKey("invitationLink")
                            .setValue(invitationLink)
                            .build())
                    .build();

            // Send email via gRPC
            EmailResponse response = stub.sendReportEmail(request);

            if (response.getSuccess()) {
                log.info("Project invitation email sent successfully via gRPC - Email ID: {}",
                        response.getEmailId());
            } else {
                log.error("Failed to send project invitation email via gRPC: {}", response.getMessage());
            }

        } catch (StatusRuntimeException e) {
            log.error("gRPC error sending project invitation email: {}", e.getStatus(), e);
            throw new RuntimeException("Email service unavailable", e);
        }
    }

    private void sendVerificationEmail(User user, String token) {
        try {
            String verificationLink = verificationUrl + "?token=" + token;

            // Generate 6-digit verification code
            String verificationCode = String.format("%06d", (int) (Math.random() * 1000000));

            // Create gRPC stub
            EmailServiceGrpc.EmailServiceBlockingStub stub = EmailServiceGrpc.newBlockingStub(emailServiceChannel);

            // Build request
            VerificationEmailRequest request = VerificationEmailRequest.newBuilder()
                    .setTo(user.getEmail())
                    .setUsername(user.getFullName())
                    .setVerificationCode(verificationCode)
                    .setVerificationLink(verificationLink)
                    .build();

            // Send email via gRPC
            EmailResponse response = stub.sendVerificationEmail(request);

            if (response.getSuccess()) {
                log.info("Verification email sent successfully via gRPC - Email ID: {}",
                        response.getEmailId());
            } else {
                log.error("Failed to send verification email via gRPC: {}", response.getMessage());
            }

        } catch (StatusRuntimeException e) {
            log.error("gRPC error sending verification email: {}", e.getStatus(), e);
            throw new RuntimeException("Email service unavailable", e);
        }
    }

    private void sendPasswordResetEmail(User user, String token) {
        try {
            String resetLink = verificationUrl.replace("/verify", "/reset-password") + "?token=" + token;

            // Generate 6-digit verification code for password reset
            String verificationCode = String.format("%06d", (int) (Math.random() * 1000000));

            // Create gRPC stub
            EmailServiceGrpc.EmailServiceBlockingStub stub = EmailServiceGrpc.newBlockingStub(emailServiceChannel);

            // Build request (reusing verification email for password reset)
            VerificationEmailRequest request = VerificationEmailRequest.newBuilder()
                    .setTo(user.getEmail())
                    .setUsername(user.getFullName())
                    .setVerificationCode(verificationCode)
                    .setVerificationLink(resetLink)
                    .build();

            // Send email via gRPC
            EmailResponse response = stub.sendVerificationEmail(request);

            if (response.getSuccess()) {
                log.info("Password reset email sent successfully via gRPC - Email ID: {}",
                        response.getEmailId());
            } else {
                log.error("Failed to send password reset email via gRPC: {}", response.getMessage());
            }

        } catch (StatusRuntimeException e) {
            log.error("gRPC error sending password reset email: {}", e.getStatus(), e);
            throw new RuntimeException("Email service unavailable", e);
        }
    }
}
