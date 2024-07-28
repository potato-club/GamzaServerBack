package gamza.project.gamzaweb.Entity;

import gamza.project.gamzaweb.Entity.Enums.ApplicationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // 엔티티 추가
    private String imageId;

    @Column(nullable = false) // 엔티티 추가
    private String containerId;

    @Column(length = 30, nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private int port;

    @Column(nullable = false)
    private int internalPort;

    @ManyToOne()
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @Enumerated(EnumType.STRING)
    private ApplicationType type;
}
