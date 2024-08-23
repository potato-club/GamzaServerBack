package gamza.project.gamzaweb.Dto.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProjectPerResponseDto {

    private String name;
    private boolean approveState;
//    private int internalPort;
//    private String zip;
}
