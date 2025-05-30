package gamza.project.gamzaweb.dto.docker;

import gamza.project.gamzaweb.Entity.ApplicationEntity;
import lombok.Data;

@Data
public class RequestDockerContainerDto {

    private String name;
    private int outerPort;
    private int internalPort;
    private String tag;

    public ApplicationEntity toEntity() {
        return ApplicationEntity.builder()
//                .name(name)
                .outerPort(outerPort)
                .internalPort(internalPort)
                .tag(tag)
                .build();
    }
}
