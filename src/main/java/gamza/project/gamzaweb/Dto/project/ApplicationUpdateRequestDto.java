package gamza.project.gamzaweb.Dto.project;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationUpdateRequestDto {
    private int outerPort;
    private String tag;
    private String variableKey;
}
