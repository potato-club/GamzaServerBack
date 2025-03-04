package gamza.project.gamzaweb.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContainerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String containerId;

    @Column(nullable = false)
    private String imageId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

//    @ManyToOne
//    @JoinColumn(name = "project_id", nullable = false)
//    private ProjectEntity project;

    @OneToOne
    @JoinColumn(name = "application_id", nullable = false) // application_id를 외래 키로 설정
    private ApplicationEntity application;

}
