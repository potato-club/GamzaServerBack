package gamza.project.gamzaweb.Repository;

import gamza.project.gamzaweb.Entity.PlatformEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformRepository extends JpaRepository<PlatformEntity, Long> {

    PlatformEntity findByPlatformName(String platformName);
}
