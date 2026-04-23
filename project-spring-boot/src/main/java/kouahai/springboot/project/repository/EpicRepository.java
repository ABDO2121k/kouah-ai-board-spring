package kouahai.springboot.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kouahai.springboot.project.model.Epic;
import kouahai.springboot.project.model.ProductBacklog;

public interface EpicRepository extends JpaRepository<Epic, Long> {

    List<Epic> findByProductBacklog(ProductBacklog backlog);

    List<Epic> findByProductBacklogOrderByIdAsc(ProductBacklog backlog);
}
