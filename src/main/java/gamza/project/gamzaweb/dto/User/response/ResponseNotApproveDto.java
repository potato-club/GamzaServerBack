package gamza.project.gamzaweb.dto.User.response;


import gamza.project.gamzaweb.Entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ResponseNotApproveDto {

    private final Long id;
    private final String major;
    private final String email;
    private final String name;

    public ResponseNotApproveDto(UserEntity userEntity) {
        this.id = userEntity.getId();
        this.major = userEntity.getMajor();
        this.email = userEntity.getEmail();
        this.name = userEntity.getFamilyName() + userEntity.getGivenName();
    }
}
