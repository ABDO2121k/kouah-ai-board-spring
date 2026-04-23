package kouahai.springboot.project.service.kanban;

import java.util.List;

import kouahai.springboot.project.dto.kanban.CreateKanbanColumnRequest;
import kouahai.springboot.project.dto.kanban.KanbanColumnResponse;
import kouahai.springboot.project.dto.kanban.UpdateKanbanColumnRequest;

public interface KanbanService {

    KanbanColumnResponse createColumn(Long sprintId, CreateKanbanColumnRequest request, Long userId);

    KanbanColumnResponse getColumnById(Long columnId, Long userId);

    List<KanbanColumnResponse> getSprintColumns(Long sprintId, Long userId);

    KanbanColumnResponse updateColumn(Long columnId, UpdateKanbanColumnRequest request, Long userId);

    void deleteColumn(Long columnId, Long userId);

    List<KanbanColumnResponse> reorderColumns(Long sprintId, List<Long> columnIds, Long userId);
}
