package kouahai.springboot.project.repository;

import kouahai.springboot.project.model.InvitationStatus;
import kouahai.springboot.project.model.Project;
import kouahai.springboot.project.model.ProjectInvitation;
import kouahai.springboot.project.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectInvitationRepository extends JpaRepository<ProjectInvitation, Long> {

    Optional<ProjectInvitation> findByInvitationToken(String invitationToken);

    Optional<ProjectInvitation> findByProjectAndUserAndStatus(Project project, User user, InvitationStatus status);

    List<ProjectInvitation> findByUserAndStatus(User user, InvitationStatus status);

    List<ProjectInvitation> findByProjectAndStatus(Project project, InvitationStatus status);

    boolean existsByProjectAndUser(Project project, User user);
}
