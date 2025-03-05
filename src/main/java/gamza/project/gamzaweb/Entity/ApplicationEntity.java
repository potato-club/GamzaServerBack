package gamza.project.gamzaweb.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageId; // zip

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

    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private ContainerEntity containerEntity;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileEntity> fileEntities = new ArrayList<>();

    public void updateDockerfilePath(String dockerFilePath) {
        this.imageId = dockerFilePath;
    }

    public void updateApplication(String newFilePath, int outerPort, String tag, String variableKey) {
        if (newFilePath != null && !newFilePath.isEmpty()) {
            this.imageId = newFilePath;
        }
        this.outerPort = outerPort;
        this.tag = tag;
        this.variableKey = variableKey;
    }
}
