package gamza.project.gamzaweb.service.Interface;

import gamza.project.gamzaweb.dto.platform.request.PlatformCreateRequestDto;
import gamza.project.gamzaweb.dto.platform.response.PlatformListResponseDto;
import jakarta.servlet.http.HttpServletRequest;

public interface PlatformService {

    PlatformListResponseDto getAllPlatformList(HttpServletRequest request);

    void deletePlatform(HttpServletRequest request, Long id);

    void createPlatform(HttpServletRequest request, PlatformCreateRequestDto dto);
}
