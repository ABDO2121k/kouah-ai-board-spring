package kouahai.springboot.project.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "sprints")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String sprintGoal;
    private boolean isActive;

    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserStory> userStories = new ArrayList<>();

    @OneToMany(mappedBy = "sprint", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<KanbanColumn> kanbanColumns = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "sprint_members", joinColumns = @JoinColumn(name = "sprint_id"), inverseJoinColumns = @JoinColumn(name = "project_member_id"))
    private Set<ProjectMember> members = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "project_id")
    private Project project;
}
