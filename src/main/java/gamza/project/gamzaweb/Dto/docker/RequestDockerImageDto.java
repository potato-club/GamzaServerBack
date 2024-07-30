package gamza.project.gamzaweb.Dto.docker;

import gamza.project.gamzaweb.Entity.ImageEntity;
import lombok.Data;

@Data
public class RequestDockerImageDto {

    private String name;
    private String tag;
    private String key;


}
