package gamza.project.gamzaweb.Entity;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity extends BaseTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(length = 10, nullable = false)
    private String familyName;

    @Column(length = 10, nullable = false)
    private String givenName;

    @Column(length = 50, nullable = false)
    private String email;

    @Column(length = 30)
    private String major;

    @Column(nullable = false, unique = true)
    private String student_number;

    @OneToMany(mappedBy = "leader")
    private List<ProjectEntity> projects;
}
