package gamza.project.gamzaweb.Entity;

import gamza.project.gamzaweb.Entity.Enums.*;
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
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    @Builder.Default
    private List<CollaboratorEntity> collaborators = new ArrayList<>();

    @OneToOne(cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, fetch = FetchType.LAZY)  // Cascade 설정으로 함께 저장/삭제 가능
    @JoinColumn(name = "application_id")  // 외래 키 컬럼명 설정
    private ApplicationEntity application;

    @ManyToOne
    @JoinColumn(name = "platform_id", nullable = false)
    private PlatformEntity platformEntity;

    @Enumerated(EnumType.STRING)
    private ProjectType projectType;

    private LocalDate startedDate;
    private LocalDate endedDate;

    @Enumerated(EnumType.STRING)
    private ApprovalProjectStatus approvalProjectStatus; // 이미지 생성 성공,실패,진행중 여부

    private String deploymentStep; // 이미지 생성 step

    @Column(nullable = false)
    private boolean approveState; // -> 프로젝트 승인 상태

    @Column(nullable = false)
    private boolean fixedState; // 수정요청 여부

    @Column(nullable = false)
    private boolean approveFixedState; //수정요청승인여부

    @Column
    private boolean successCheck; // 성공한 프로젝트 확인여부

    public void updateProject(String name, String description, ProjectState state, LocalDate startedDate, LocalDate endedDate, List<CollaboratorEntity> collaborators, ProjectType projectType) {
        this.name = name;
        this.description = description;
        this.state = state;
        this.startedDate = startedDate;
        this.endedDate = endedDate;
        this.collaborators = collaborators;
        this.projectType = projectType;
    }

    public void addProjectCollaborator(List<CollaboratorEntity> collaborators) {
        this.collaborators.addAll(collaborators);
    }

    public void approveCreateProject() {
        this.approveState = true;
    }

    public void updateFixedState() {
        this.fixedState = true;
    }

    public void approveFixedProject() {
        this.approveFixedState = true;
    }

    public boolean isCollaborator(Long userId) {
        return collaborators.stream()
                .anyMatch(collaborator -> collaborator.getUser().getId().equals(userId));
    }

    public void updateApprovalStatus(ApprovalProjectStatus status) {
        this.approvalProjectStatus = status;
    }

    public void updateDeploymentStep(String step) {
        this.deploymentStep = step;
    }

    public void updateSuccessCheck() {
        this.successCheck = true;
    }

    public void updatePlatform(PlatformEntity platform) {
        this.platformEntity = platform;
    }

    public void updateApprovalProjectStatus(ApprovalProjectStatus status) {
        this.approvalProjectStatus = status;
    }

}
