package gamza.project.gamzaweb.dto.application;

import gamza.project.gamzaweb.Entity.Enums.ApplicationType;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationRequestDto {

    private ProjectEntity projectEntity;
    private String name;
    private int internalPort;
    private int outerPort;
    private String tag;
    private String variableKey;
    private ApplicationType applicationType;

}
