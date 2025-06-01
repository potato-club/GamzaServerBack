package gamza.project.gamzaweb.dto.project.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectPerResponseDto {

    private Long id;
    private String title;
    private int port;
    private String file;

    private String containerId;

}
