package kouahai.springboot.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kouahai.springboot.project.model.Project;
import kouahai.springboot.project.model.Sprint;

public interface SprintRepository extends JpaRepository<Sprint, Long> {

    List<Sprint> findByProject(Project project);

    List<Sprint> findByProjectOrderByStartDateDesc(Project project);

    Optional<Sprint> findByProjectAndIsActive(Project project, boolean isActive);

    List<Sprint> findByIsActive(boolean isActive);
}
