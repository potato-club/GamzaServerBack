package gamza.project.gamzaweb.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageId;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    private String variableKey;

    @ManyToOne()
    @JoinColumn(name = "project_id", nullable = false) // 1229 성훈 추가
    private ProjectEntity project;

    public void updatedImageId(String imageId) {
        this.imageId = imageId;
    }

}
