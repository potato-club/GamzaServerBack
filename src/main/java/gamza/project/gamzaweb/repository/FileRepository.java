package gamza.project.gamzaweb.repository;

import gamza.project.gamzaweb.Entity.ApplicationEntity;
import gamza.project.gamzaweb.Entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Long> {

//    FileEntity findByProject(ProjectEntity project);
//
    FileEntity findFirstByApplicationOrderByIdDesc(ApplicationEntity application);

}
