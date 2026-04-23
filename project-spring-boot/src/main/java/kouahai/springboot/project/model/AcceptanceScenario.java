package kouahai.springboot.project.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "acceptance_scenarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcceptanceScenario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 1000)
    private String givenCondition;

    @Column(length = 1000)
    private String whenAction;

    @Column(length = 1000)
    private String thenResult;

    @ManyToOne
    @JoinColumn(name = "user_story_id")
    private UserStory userStory;
}
