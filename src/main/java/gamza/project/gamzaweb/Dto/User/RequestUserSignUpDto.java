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
    private String student_number;

    @Builder
    public UserEntity toEntity() {
        return UserEntity.builder()
                .familyName(familyName)
                .givenName(givenName)
                .email(email)
                .password(password)
                .userRole(UserRole.USER) // default ROLE_USER
                .major(major)
                .student_number(student_number)
                .build();
    }

}