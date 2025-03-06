package gamza.project.gamzaweb.Service.Interface;

import gamza.project.gamzaweb.Dto.platform.PlatformCreateRequestDto;
import gamza.project.gamzaweb.Dto.platform.PlatformListResponseDto;
import gamza.project.gamzaweb.Entity.PlatformEntity;
import jakarta.servlet.http.HttpServletRequest;

public interface PlatformService {

    PlatformListResponseDto getAllPlatformList(HttpServletRequest request);

    void deletePlatform(HttpServletRequest request, Long id);

    void createPlatform(HttpServletRequest request, PlatformCreateRequestDto dto);
}
