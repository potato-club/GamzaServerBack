package gamza.project.gamzaweb.dto.user.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseCollaboratorDto {
    private Long id;
    private String name;
    private String studentId;
}
