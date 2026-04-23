package kouahai.springboot.project.service.epic;

import java.util.List;

import kouahai.springboot.project.dto.epic.CreateEpicRequest;
import kouahai.springboot.project.dto.epic.EpicResponse;
import kouahai.springboot.project.dto.epic.UpdateEpicRequest;

public interface EpicService {

    EpicResponse createEpic(Long projectId, CreateEpicRequest request, Long userId);

    EpicResponse getEpicById(Long epicId, Long userId);

    List<EpicResponse> getProjectEpics(Long projectId, Long userId);

    EpicResponse updateEpic(Long epicId, UpdateEpicRequest request, Long userId);

    void deleteEpic(Long epicId, Long userId);
}
