package gamza.project.gamzaweb.Entity;

import gamza.project.gamzaweb.Entity.Enums.ProjectState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectEntity extends BaseTime{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Long id;

    @Column(length = 100, nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    private ProjectState state;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectLinkEntity> links;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity leader;

    private LocalDateTime startedDate;
    private LocalDateTime endedDate;

//    private ApplicationEntity content;

//    private List<FileEntity> images = new ArrayList<>(); // 추후 추가


}
