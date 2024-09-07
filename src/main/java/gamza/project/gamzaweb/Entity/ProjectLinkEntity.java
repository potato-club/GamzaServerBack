//package gamza.project.gamzaweb.Entity;
//
//import gamza.project.gamzaweb.Entity.Enums.ProjectLinkType;
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//@Entity
//@Getter
//@NoArgsConstructor
//@AllArgsConstructor
//public class ProjectLinkEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @ManyToOne
//    @JoinColumn(name = "project_id", nullable = false)
//    private ProjectEntity project;
//
//    @Enumerated(EnumType.STRING)
//    private ProjectLinkType type;
//
//    @Column(nullable = false)
//    private String url;
//
//}
