package kouahai.springboot.project.dto.userstory;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kouahai.springboot.project.model.Priority;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserStoryRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 200, message = "Title must be between 3 and 200 characters")
    private String title;

    @NotBlank(message = "Role is required (As a ...)")
    @Size(max = 100)
    private String asRole;

    @NotBlank(message = "'I want' is required")
    @Size(max = 500)
    private String iWant;

    @NotBlank(message = "'So that' is required")
    @Size(max = 500)
    private String soThat;

    private Priority priority;

    private Long epicId; // Optional: can be null if story doesn't belong to an epic
}
