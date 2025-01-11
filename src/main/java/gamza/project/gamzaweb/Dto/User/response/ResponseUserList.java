package gamza.project.gamzaweb.Dto.User.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseUserList {

    private List<ResponseUser> userList;

}
