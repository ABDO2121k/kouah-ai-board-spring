package kouahai.springboot.project.dto.kanban;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateKanbanColumnRequest {

    @NotBlank(message = "Column name is required")
    @Size(min = 2, max = 50, message = "Column name must be between 2 and 50 characters")
    private String name;

    @NotNull(message = "Position index is required")
    private Integer positionIndex;

    private boolean isDoneStatus = false;

    private boolean testerOnly = false;

    @Pattern(regexp = "^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$", message = "Color must be a valid hexadecimal color code (e.g., #FF5733 or #F57)")
    private String color; 
}
