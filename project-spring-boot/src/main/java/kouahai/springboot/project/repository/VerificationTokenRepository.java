package kouahai.springboot.project.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kouahai.springboot.project.model.TokenType;
import kouahai.springboot.project.model.User;
import kouahai.springboot.project.model.VerificationToken;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);

    List<VerificationToken> findByUser(User user);

    List<VerificationToken> findByUserAndTokenType(User user, TokenType tokenType);

    void deleteByExpiresAtBefore(LocalDateTime dateTime);

    Optional<VerificationToken> findByTokenAndTokenType(String token, TokenType tokenType);
}
