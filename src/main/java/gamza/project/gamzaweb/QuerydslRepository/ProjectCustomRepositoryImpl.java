package gamza.project.gamzaweb.QuerydslRepository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.Entity.QImageEntity;
import gamza.project.gamzaweb.Entity.QProjectEntity;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class ProjectCustomRepositoryImpl implements ProjectCustomRepository {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ProjectEntity> findProjectsWithImages() {
        QProjectEntity project = QProjectEntity.projectEntity;
        QImageEntity image = QImageEntity.imageEntity;

        return jpaQueryFactory
                .selectFrom(project)
                .join(project.imageEntity, image).fetchJoin()
                .where(project.approveState.eq(true)
                        .and(project.fixedState.eq(false)
                                .or(project.fixedState.eq(true)
                                        .and(project.approveFixedState.eq(true))
                                )
                        )
                        .and(image.imageId.isNotNull())
                )
                .distinct()
                .orderBy(project.updatedDate.desc())
                .fetch();
    }


}
