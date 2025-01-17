package gamza.project.gamzaweb.Entity;

import gamza.project.gamzaweb.Entity.Enums.BaseTime;
import gamza.project.gamzaweb.Entity.Enums.ProjectState;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
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

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true) // mappedBy 설정 추가
    private List<ImageEntity> imageEntity;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity leader; // 프로젝트 생성자가 팀장임

    @Column()
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<CollaboratorEntity> collaborators = new ArrayList<>();

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY)  // Cascade 설정으로 함께 저장/삭제 가능
    @JoinColumn(name = "application_id")  // 외래 키 컬럼명 설정
    private ApplicationEntity application;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileEntity> fileEntities = new ArrayList<>();

    private LocalDate startedDate;
    private LocalDate endedDate;

    @Column(nullable = false)
    private boolean approveState; // -> 프로젝트 승인 상태

    @Column(nullable = false)
    private boolean fixedState;

    @Column(nullable = false)
    private boolean approveFixedState;

    public void updateProject(String name, String description, ProjectState state, LocalDate startedDate, LocalDate endedDate, List<CollaboratorEntity> collaborators) {
        this.name = name;
        this.description = description;
        this.state = state;
        this.startedDate = startedDate;
        this.endedDate = endedDate;
        this.collaborators = collaborators;
    }

    public void addProjectCollaborator(List<CollaboratorEntity> collaborators) {
        this.collaborators.addAll(collaborators);
    }

    public void approveCreateProject() {
        this.approveState = true;
    }

    public void approveFixedProject() {
        this.approveFixedState = true;
    }

    public boolean isCollaborator(Long userId) {
        return collaborators.stream()
                .anyMatch(collaborator -> collaborator.getUser().getId().equals(userId));
    }

}
