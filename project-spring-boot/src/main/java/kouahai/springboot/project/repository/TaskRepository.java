package kouahai.springboot.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kouahai.springboot.project.model.KanbanColumn;
import kouahai.springboot.project.model.ProjectMember;
import kouahai.springboot.project.model.Task;
import kouahai.springboot.project.model.UserStory;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUserStory(UserStory userStory);

    List<Task> findByKanbanColumn(KanbanColumn column);

    List<Task> findByAssigneesContaining(ProjectMember member);

    List<Task> findByUserStoryAndKanbanColumn(UserStory userStory, KanbanColumn column);
}
