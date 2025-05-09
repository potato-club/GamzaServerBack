package gamza.project.gamzaweb.dto.docker;

import lombok.Data;

@Data
public class RequestDockerContainerDeleteDto {
    private String containerId;
}
