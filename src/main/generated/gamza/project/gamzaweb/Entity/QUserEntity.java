package gamza.project.gamzaweb.Entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserEntity is a Querydsl query type for UserEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserEntity extends EntityPathBase<UserEntity> {

    private static final long serialVersionUID = -1376912754L;

    public static final QUserEntity userEntity = new QUserEntity("userEntity");

    public final ListPath<ContainerEntity, QContainerEntity> containerEntities = this.<ContainerEntity, QContainerEntity>createList("containerEntities", ContainerEntity.class, QContainerEntity.class, PathInits.DIRECT2);

    public final StringPath email = createString("email");

    public final StringPath familyName = createString("familyName");

    public final StringPath givenName = createString("givenName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath major = createString("major");

    public final StringPath password = createString("password");

    public final ListPath<ProjectEntity, QProjectEntity> projects = this.<ProjectEntity, QProjectEntity>createList("projects", ProjectEntity.class, QProjectEntity.class, PathInits.DIRECT2);

    public final StringPath studentId = createString("studentId");

    public final EnumPath<gamza.project.gamzaweb.Entity.Enums.UserRole> userRole = createEnum("userRole", gamza.project.gamzaweb.Entity.Enums.UserRole.class);

    public QUserEntity(String variable) {
        super(UserEntity.class, forVariable(variable));
    }

    public QUserEntity(Path<? extends UserEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUserEntity(PathMetadata metadata) {
        super(UserEntity.class, metadata);
    }

}

