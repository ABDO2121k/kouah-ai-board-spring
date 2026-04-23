package kouahai.springboot.project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kouahai.springboot.project.model.User;
import kouahai.springboot.project.model.UserStatus;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByUsernameAndStatus(String username, UserStatus status);
}
