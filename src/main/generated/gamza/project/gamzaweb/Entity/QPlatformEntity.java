package gamza.project.gamzaweb.Entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPlatformEntity is a Querydsl query type for PlatformEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPlatformEntity extends EntityPathBase<PlatformEntity> {

    private static final long serialVersionUID = -1689986762L;

    public static final QPlatformEntity platformEntity = new QPlatformEntity("platformEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath platformName = createString("platformName");

    public final ListPath<ProjectEntity, QProjectEntity> projects = this.<ProjectEntity, QProjectEntity>createList("projects", ProjectEntity.class, QProjectEntity.class, PathInits.DIRECT2);

    public QPlatformEntity(String variable) {
        super(PlatformEntity.class, forVariable(variable));
    }

    public QPlatformEntity(Path<? extends PlatformEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPlatformEntity(PathMetadata metadata) {
        super(PlatformEntity.class, metadata);
    }

}

