package gamza.project.gamzaweb.Service.Interface;

import gamza.project.gamzaweb.Entity.PlatformEntity;

public interface PlatformService {

    PlatformEntity checkedOrMakePlatform(String platformName, Long platformId);
}
