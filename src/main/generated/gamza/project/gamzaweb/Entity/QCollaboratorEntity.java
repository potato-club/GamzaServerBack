package gamza.project.gamzaweb.Entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCollaboratorEntity is a Querydsl query type for CollaboratorEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCollaboratorEntity extends EntityPathBase<CollaboratorEntity> {

    private static final long serialVersionUID = 675980937L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCollaboratorEntity collaboratorEntity = new QCollaboratorEntity("collaboratorEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QProjectEntity project;

    public final QUserEntity user;

    public QCollaboratorEntity(String variable) {
        this(CollaboratorEntity.class, forVariable(variable), INITS);
    }

    public QCollaboratorEntity(Path<? extends CollaboratorEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCollaboratorEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCollaboratorEntity(PathMetadata metadata, PathInits inits) {
        this(CollaboratorEntity.class, metadata, inits);
    }

    public QCollaboratorEntity(Class<? extends CollaboratorEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.project = inits.isInitialized("project") ? new QProjectEntity(forProperty("project"), inits.get("project")) : null;
        this.user = inits.isInitialized("user") ? new QUserEntity(forProperty("user")) : null;
    }

}

