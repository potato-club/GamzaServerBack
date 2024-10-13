package gamza.project.gamzaweb.Dto.project;

import gamza.project.gamzaweb.Entity.ApplicationEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDetailResponseDto {
    private Long id;
    private String file;
    private int port;
    private String tag;
    private String variableKey;

    public ApplicationDetailResponseDto(ApplicationEntity application) {
        this.id = application.getId();
        this.file = application.getImageId();
        this.port = application.getOuterPort();
        this.tag = application.getTag();
        this.variableKey = application.getVariableKey();
    }
}
