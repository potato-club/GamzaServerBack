package gamza.project.gamzaweb.Entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QApplicationEntity is a Querydsl query type for ApplicationEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QApplicationEntity extends EntityPathBase<ApplicationEntity> {

    private static final long serialVersionUID = -761743789L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QApplicationEntity applicationEntity = new QApplicationEntity("applicationEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageId = createString("imageId");

    public final NumberPath<Integer> internalPort = createNumber("internalPort", Integer.class);

    public final NumberPath<Integer> outerPort = createNumber("outerPort", Integer.class);

    public final QProjectEntity project;

    public final StringPath tag = createString("tag");

    public final StringPath variableKey = createString("variableKey");

    public QApplicationEntity(String variable) {
        this(ApplicationEntity.class, forVariable(variable), INITS);
    }

    public QApplicationEntity(Path<? extends ApplicationEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QApplicationEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QApplicationEntity(PathMetadata metadata, PathInits inits) {
        this(ApplicationEntity.class, metadata, inits);
    }

    public QApplicationEntity(Class<? extends ApplicationEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.project = inits.isInitialized("project") ? new QProjectEntity(forProperty("project"), inits.get("project")) : null;
    }

}

