package gamza.project.gamzaweb.Dto.User;


import gamza.project.gamzaweb.Entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseNotApproveDto {

    private Long id;
    private String familyName;
    private String givenName;
    // github link ?

    public ResponseNotApproveDto(UserEntity userEntity) {
        this.id = userEntity.getId();
        this.familyName = userEntity.getFamilyName();
        this.givenName = userEntity.getGivenName();
    }
}
