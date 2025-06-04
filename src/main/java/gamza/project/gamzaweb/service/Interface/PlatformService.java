package gamza.project.gamzaweb.service.Interface;

import gamza.project.gamzaweb.dto.platform.request.PlatformCreateRequestDto;
import gamza.project.gamzaweb.dto.platform.response.PlatformListResponseDto;
import jakarta.servlet.http.HttpServletRequest;

public interface PlatformService {

    PlatformListResponseDto getAllPlatformList();

    void deletePlatform(Long id);

    void createPlatform(PlatformCreateRequestDto dto);
}
