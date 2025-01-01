package gamza.project.gamzaweb.Entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QImageEntity is a Querydsl query type for ImageEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QImageEntity extends EntityPathBase<ImageEntity> {

    private static final long serialVersionUID = -366828002L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QImageEntity imageEntity = new QImageEntity("imageEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath imageId = createString("imageId");

    public final StringPath name = createString("name");

    public final QProjectEntity project;

    public final QUserEntity user;

    public final StringPath variableKey = createString("variableKey");

    public QImageEntity(String variable) {
        this(ImageEntity.class, forVariable(variable), INITS);
    }

    public QImageEntity(Path<? extends ImageEntity> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QImageEntity(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QImageEntity(PathMetadata metadata, PathInits inits) {
        this(ImageEntity.class, metadata, inits);
    }

    public QImageEntity(Class<? extends ImageEntity> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.project = inits.isInitialized("project") ? new QProjectEntity(forProperty("project"), inits.get("project")) : null;
        this.user = inits.isInitialized("user") ? new QUserEntity(forProperty("user")) : null;
    }

}

