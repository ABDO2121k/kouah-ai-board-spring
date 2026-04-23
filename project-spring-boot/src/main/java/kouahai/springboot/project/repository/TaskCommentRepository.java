package kouahai.springboot.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import kouahai.springboot.project.model.ProjectMember;
import kouahai.springboot.project.model.Task;
import kouahai.springboot.project.model.TaskComment;

public interface TaskCommentRepository extends JpaRepository<TaskComment, Long> {

    List<TaskComment> findByTask(Task task);

    List<TaskComment> findByTaskOrderByCreatedAtDesc(Task task);

    List<TaskComment> findByAuthor(ProjectMember author);
}
