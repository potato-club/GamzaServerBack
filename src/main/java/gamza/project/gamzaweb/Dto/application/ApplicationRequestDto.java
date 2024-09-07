package gamza.project.gamzaweb.Dto.application;

import gamza.project.gamzaweb.Dto.project.ProjectRequestDto;
import gamza.project.gamzaweb.Entity.Enums.ApplicationType;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationRequestDto {

    private ProjectRequestDto projectRequestDto;
    private String name;
    private int internalPort;
    private String tag;
    private String variableKey;
    private ApplicationType applicationType;

}
