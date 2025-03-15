package gamza.project.gamzaweb.repository;

import gamza.project.gamzaweb.Entity.ImageEntity;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<ImageEntity, Long> {

    Optional<ImageEntity> findByImageIdAndUser(String imageId, UserEntity user);

    Optional<ImageEntity> findByImageId(String imageId);

    Optional<ImageEntity> findByNameAndUser(String name, UserEntity user);

    Optional<ImageEntity> findByProjectAndUser(ProjectEntity project, UserEntity user);


}
