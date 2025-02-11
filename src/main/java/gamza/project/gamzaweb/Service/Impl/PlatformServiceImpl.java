package gamza.project.gamzaweb.Service.Impl;

import gamza.project.gamzaweb.Dto.platform.PlatformListResponseDto;
import gamza.project.gamzaweb.Dto.platform.PlatformResponseDto;
import gamza.project.gamzaweb.Entity.PlatformEntity;
import gamza.project.gamzaweb.Error.ErrorCode;
import gamza.project.gamzaweb.Error.requestError.BadRequestException;
import gamza.project.gamzaweb.Repository.PlatformRepository;
import gamza.project.gamzaweb.Service.Interface.PlatformService;
import gamza.project.gamzaweb.Validate.UserValidate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformServiceImpl implements PlatformService {

    private final UserValidate userValidate;
    private final PlatformRepository platformRepository;

    @Override
    public PlatformEntity checkedOrMakePlatform(String platformName, Long platformId) {

        if(platformName.isBlank() && platformId != null) {

            return platformRepository.findById(platformId)
                    .orElseThrow(() -> new BadRequestException("잘못된 플랫폼 ID값입니다.", ErrorCode.INTERNAL_SERVER_EXCEPTION));
        }
        if(!platformName.isBlank() && platformId == null) {
            PlatformEntity newPlatform = PlatformEntity.builder()
                    .platformName(platformName)
                    .build();
            platformRepository.save(newPlatform);
            return newPlatform;
        }

        return null;
    }

    @Override
    public PlatformListResponseDto getAllPlatformList(HttpServletRequest request) {
        userValidate.validateUserRole(request);

        List<PlatformEntity> platformEntities = platformRepository.findAll(); // 무슨 순으로 해주지?

        List<PlatformResponseDto> platformResponseDtoList = platformEntities.stream()
                .map(platformEntity -> PlatformResponseDto.builder()
                        .platformId(platformEntity.getId())
                        .platformName(platformEntity.getPlatformName())
                        .build())
                .toList();

        return PlatformListResponseDto.builder()
                .platformResponseDtos(platformResponseDtoList)
                .build();
    }

}
