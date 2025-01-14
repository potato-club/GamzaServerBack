package gamza.project.gamzaweb.Dto.User.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseUser {

    private Long id;
    private String name;
    private String studentId;

}
