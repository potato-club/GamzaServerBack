package gamza.project.gamzaweb.Entity;

import gamza.project.gamzaweb.Entity.Enums.ApplicationType;
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
public class ApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne
//    @JoinColumn(name = "user_id")
//    private UserEntity user;

    private String imageId; // zip

//    @Column(length = 30, nullable = false, unique = true)
//    private String name; // application Name

    @Column(nullable = false, unique = true)
    private int outerPort;

    @Column(nullable = false)
    private int internalPort;

    @Column(nullable = false)
    private String tag;

    @Column(nullable = true)
    private String variableKey;

    @OneToOne(mappedBy = "application", fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id")  // 외래 키 컬럼명 설정
    private ProjectEntity project;

//    @Enumerated(EnumType.STRING)
//    private ApplicationType type;

    public void updateDockerfilePath(String dockerFilePath) {
        this.imageId = dockerFilePath;
    }

}
