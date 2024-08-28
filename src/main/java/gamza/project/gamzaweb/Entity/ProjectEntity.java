package gamza.project.gamzaweb.Entity;

import gamza.project.gamzaweb.Entity.Enums.ProjectState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectEntity extends BaseTime {

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

    private LocalDate startedDate;
    private LocalDate endedDate;

    @Column(nullable = false)
    private boolean approveState; // -> 프로젝트 승인 상태

    public void updateProject(String name, String description, ProjectState state, LocalDate startedDate, LocalDate endedDate) {
        this.name = name;
        this.description = description;
        this.state = state;
        this.startedDate = startedDate;
        this.endedDate = endedDate;
    }

    public void approveCreateProject() {
        this.approveState = true;
    }
//    private ApplicationEntity content;

//    private List<FileEntity> images = new ArrayList<>(); // 추후 추가


}
