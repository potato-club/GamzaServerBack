package gamza.project.gamzaweb.Entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QContainerEntity is a Querydsl query type for ContainerEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QContainerEntity extends EntityPathBase<ContainerEntity> {

    private static final long serialVersionUID = -749120316L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QContainerEntity containerEntity = new QContainerEntity("containerEntity");

    public final StringPath containerId = createString("containerId");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageId = createString("imageId");

    public final QUserEntity user;

    public QContainerEntity(String variable) {
        this(ContainerEntity.class, forVariable(variable), INITS);
    }

    public QContainerEntity(Path<? extends ContainerEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QContainerEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QContainerEntity(PathMetadata metadata, PathInits inits) {
        this(ContainerEntity.class, metadata, inits);
    }

    public QContainerEntity(Class<? extends ContainerEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUserEntity(forProperty("user")) : null;
    }

}

