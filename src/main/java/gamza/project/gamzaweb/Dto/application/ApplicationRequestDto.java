package gamza.project.gamzaweb.Dto.application;

import gamza.project.gamzaweb.Entity.Enums.ApplicationType;
import gamza.project.gamzaweb.Entity.VariableEntity;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationRequestDto {

    private String imageId;
    private String name;
    private int internalPort;
    private String tag;
    private VariableEntity variableKey;
    private ApplicationType applicationType;

}
