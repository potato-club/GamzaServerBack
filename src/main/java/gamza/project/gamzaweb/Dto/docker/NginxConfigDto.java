package gamza.project.gamzaweb.dto.docker;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NginxConfigDto {
    private String containerId;
    private String port;
    private String cname;
}