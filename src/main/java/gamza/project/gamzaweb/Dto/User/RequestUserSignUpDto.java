package gamza.project.gamzaweb.Dto.User;

import gamza.project.gamzaweb.Entity.Enums.UserRole;
import gamza.project.gamzaweb.Entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestUserSignUpDto {

    private String familyName;
    private String givenName;
    private String email;
    private String password;
    private UserRole userRole;
    private String major;
    private String studentId;

    @Builder
    public UserEntity toEntity() {
        return UserEntity.builder()
                .familyName(familyName)
                .givenName(givenName)
                .email(email)
                .password(password)
                .userRole(UserRole.USER)
                .major(major)
                .studentId(studentId)
                .build();
    }

}
