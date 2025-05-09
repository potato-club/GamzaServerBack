package gamza.project.gamzaweb.dto.docker;

import gamza.project.gamzaweb.Entity.ApplicationEntity;
import gamza.project.gamzaweb.Entity.ImageEntity;
import lombok.Data;

@Data
public class RequestDockerImageDto {

    private String name;
    private String tag;
    private String key;
//    private String dockerfilePath;

    public ApplicationEntity toEntity() {
        return ApplicationEntity.builder()
//                .name(name)
                .tag(tag)
                .variableKey(key)
                .build();
    }


}
