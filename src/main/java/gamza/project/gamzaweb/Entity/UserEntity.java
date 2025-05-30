package gamza.project.gamzaweb.Entity;

import gamza.project.gamzaweb.Entity.Enums.UserRole;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(length = 10, nullable = false)
    private String familyName;

    @Column(length = 10, nullable = false)
    private String givenName;

    @Column(length = 50, nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private UserRole userRole;

    @Column(length = 30)
    private String major;

    @Column(nullable = false, unique = true)
    private String studentId;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CollaboratorEntity> projects = new ArrayList<>();

    public void approveUserStatus() {
        this.userRole = UserRole.MEMBER;
    }

    public void notApproveUserStatus() {
        this.userRole = UserRole.BLACK_USER;
    }
}
