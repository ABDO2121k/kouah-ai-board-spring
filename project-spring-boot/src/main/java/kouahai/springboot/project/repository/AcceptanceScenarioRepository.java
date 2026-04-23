package kouahai.springboot.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kouahai.springboot.project.model.AcceptanceScenario;
import kouahai.springboot.project.model.UserStory;

public interface AcceptanceScenarioRepository extends JpaRepository<AcceptanceScenario, Long> {

    List<AcceptanceScenario> findByUserStory(UserStory userStory);
}