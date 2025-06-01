package gamza.project.gamzaweb.dto.project.request;

import gamza.project.gamzaweb.Entity.Enums.DeploymentStep;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeploymentUpdateRequest {

    private Long id;
    private DeploymentStep step;

}
