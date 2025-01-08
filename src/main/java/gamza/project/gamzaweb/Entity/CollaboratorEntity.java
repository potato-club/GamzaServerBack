package gamza.project.gamzaweb.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CollaboratorEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    //    private String role; // 팀 내 역할을 지정해주면 될듯 추후 따로 권한을 만들긴 해야하는데 이건 2차 개발로 넘어가기에 주석처리
    public CollaboratorEntity(ProjectEntity project, UserEntity user) {
        this.project = project;
        this.user = user;
    }



}
