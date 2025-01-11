package gamza.project.gamzaweb.Dto.User.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestUserLoginDto {
    private String email;
    private String password;
}
