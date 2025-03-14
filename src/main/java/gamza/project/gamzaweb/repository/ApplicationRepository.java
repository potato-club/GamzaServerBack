package gamza.project.gamzaweb.repository;

import gamza.project.gamzaweb.Entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, Long>  {
}
