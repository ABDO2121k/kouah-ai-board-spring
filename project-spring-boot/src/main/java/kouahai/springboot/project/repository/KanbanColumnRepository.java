package kouahai.springboot.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kouahai.springboot.project.model.KanbanColumn;
import kouahai.springboot.project.model.Sprint;

public interface KanbanColumnRepository extends JpaRepository<KanbanColumn, Long> {

    List<KanbanColumn> findBySprint(Sprint sprint);

    List<KanbanColumn> findBySprintOrderByPositionIndexAsc(Sprint sprint);

    List<KanbanColumn> findBySprintAndTesterOnly(Sprint sprint, boolean testerOnly);
}
