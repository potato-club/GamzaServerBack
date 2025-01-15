package gamza.project.gamzaweb.Repository;

import gamza.project.gamzaweb.Entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {
}
