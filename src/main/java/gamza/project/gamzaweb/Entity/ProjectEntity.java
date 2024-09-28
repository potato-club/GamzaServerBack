package gamza.project.gamzaweb.Entity;

import gamza.project.gamzaweb.Entity.Enums.ProjectState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.context.ApplicationListener;

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

//    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<ProjectLinkEntity> links;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity leader;

    @OneToOne(cascade = CascadeType.ALL)  // Cascade 설정으로 함께 저장/삭제 가능
    @JoinColumn(name = "application_id")  // 외래 키 컬럼명 설정
    private ApplicationEntity application;

    private LocalDate startedDate;
    private LocalDate endedDate;

    @Column(nullable = false)
    private boolean approveState; // -> 프로젝트 승인 상태

    @Column(nullable = false)
    private boolean approveFixedState;


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

    public void approveFixedProject() {
        this.approveFixedState = true;
    }



//    private ApplicationEntity content;

//    private List<FileEntity> images = new ArrayList<>(); // 추후 추가


}
