package kouahai.springboot.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kouahai.springboot.project.model.Project;
import kouahai.springboot.project.model.ProjectMember;
import kouahai.springboot.project.model.ProjectRole;
import kouahai.springboot.project.model.User;

public interface ProjectMemberRepository extends JpaRepository<ProjectMember, Long> {

    List<ProjectMember> findByProject(Project project);

    List<ProjectMember> findByUser(User user);

    List<ProjectMember> findByUserId(Long userId);

    Optional<ProjectMember> findByProjectAndUser(Project project, User user);

    Optional<ProjectMember> findByProjectAndUserId(Project project, Long userId);

    List<ProjectMember> findByProjectAndRole(Project project, ProjectRole role);

    boolean existsByProjectAndUser(Project project, User user);

    long countByProject(Project project);
}
