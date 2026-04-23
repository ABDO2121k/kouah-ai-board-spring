package kouahai.springboot.project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import kouahai.springboot.project.model.ProductBacklog;
import kouahai.springboot.project.model.Project;

public interface ProductBacklogRepository extends JpaRepository<ProductBacklog, Long> {

    Optional<ProductBacklog> findByProject(Project project);

    boolean existsByProject(Project project);
}
