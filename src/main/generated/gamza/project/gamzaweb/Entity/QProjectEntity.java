package gamza.project.gamzaweb.Entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProjectEntity is a Querydsl query type for ProjectEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProjectEntity extends EntityPathBase<ProjectEntity> {

    private static final long serialVersionUID = -1286061732L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProjectEntity projectEntity = new QProjectEntity("projectEntity");

    public final gamza.project.gamzaweb.Entity.Enums.QBaseTime _super = new gamza.project.gamzaweb.Entity.Enums.QBaseTime(this);

    public final QApplicationEntity application;

    public final EnumPath<gamza.project.gamzaweb.Entity.Enums.ApprovalProjectStatus> approvalProjectStatus = createEnum("approvalProjectStatus", gamza.project.gamzaweb.Entity.Enums.ApprovalProjectStatus.class);

    public final BooleanPath approveFixedState = createBoolean("approveFixedState");

    public final BooleanPath approveState = createBoolean("approveState");

    public final ListPath<CollaboratorEntity, QCollaboratorEntity> collaborators = this.<CollaboratorEntity, QCollaboratorEntity>createList("collaborators", CollaboratorEntity.class, QCollaboratorEntity.class, PathInits.DIRECT2);

    public final ListPath<ContainerEntity, QContainerEntity> container = this.<ContainerEntity, QContainerEntity>createList("container", ContainerEntity.class, QContainerEntity.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdDate = _super.createdDate;

    public final StringPath deploymentStep = createString("deploymentStep");

    public final StringPath description = createString("description");

    public final DatePath<java.time.LocalDate> endedDate = createDate("endedDate", java.time.LocalDate.class);

    public final BooleanPath fixedState = createBoolean("fixedState");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<ImageEntity, QImageEntity> imageEntity = this.<ImageEntity, QImageEntity>createList("imageEntity", ImageEntity.class, QImageEntity.class, PathInits.DIRECT2);

    public final QUserEntity leader;

    public final StringPath name = createString("name");

    public final QPlatformEntity platformEntity;

    public final DatePath<java.time.LocalDate> startedDate = createDate("startedDate", java.time.LocalDate.class);

    public final EnumPath<gamza.project.gamzaweb.Entity.Enums.ProjectState> state = createEnum("state", gamza.project.gamzaweb.Entity.Enums.ProjectState.class);

    public final BooleanPath successCheck = createBoolean("successCheck");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedDate = _super.updatedDate;

    public QProjectEntity(String variable) {
        this(ProjectEntity.class, forVariable(variable), INITS);
    }

    public QProjectEntity(Path<? extends ProjectEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProjectEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProjectEntity(PathMetadata metadata, PathInits inits) {
        this(ProjectEntity.class, metadata, inits);
    }

    public QProjectEntity(Class<? extends ProjectEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.application = inits.isInitialized("application") ? new QApplicationEntity(forProperty("application"), inits.get("application")) : null;
        this.leader = inits.isInitialized("leader") ? new QUserEntity(forProperty("leader")) : null;
        this.platformEntity = inits.isInitialized("platformEntity") ? new QPlatformEntity(forProperty("platformEntity")) : null;
    }

}

