package kouahai.springboot.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kouahai.springboot.project.model.Epic;
import kouahai.springboot.project.model.Priority;
import kouahai.springboot.project.model.ProductBacklog;
import kouahai.springboot.project.model.Sprint;
import kouahai.springboot.project.model.UserStory;

public interface UserStoryRepository extends JpaRepository<UserStory, Long> {

    List<UserStory> findByEpic(Epic epic);

    List<UserStory> findBySprint(Sprint sprint);

    List<UserStory> findByPriority(Priority priority);

    List<UserStory> findByEpicOrderByPriorityDesc(Epic epic);

    List<UserStory> findBySprintIsNull();

    List<UserStory> findByEpicAndSprint(Epic epic, Sprint sprint);

    List<UserStory> findByProductBacklog(ProductBacklog productBacklog);
}
