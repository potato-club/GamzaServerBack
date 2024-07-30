package gamza.project.gamzaweb.Repository;

import gamza.project.gamzaweb.Entity.InfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InfoRepository extends JpaRepository<InfoEntity, Long> {
}
