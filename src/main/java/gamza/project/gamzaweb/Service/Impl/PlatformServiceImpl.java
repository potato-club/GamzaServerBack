package gamza.project.gamzaweb.Service.Impl;

import gamza.project.gamzaweb.Dto.platform.PlatformCreateRequestDto;
import gamza.project.gamzaweb.Dto.platform.PlatformListResponseDto;
import gamza.project.gamzaweb.Dto.platform.PlatformResponseDto;
import gamza.project.gamzaweb.Entity.PlatformEntity;
import gamza.project.gamzaweb.Error.ErrorCode;
import gamza.project.gamzaweb.Error.requestError.BadRequestException;
import gamza.project.gamzaweb.Error.requestError.DuplicateException;
import gamza.project.gamzaweb.Repository.PlatformRepository;
import gamza.project.gamzaweb.Service.Interface.PlatformService;
import gamza.project.gamzaweb.Validate.UserValidate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformServiceImpl implements PlatformService {

    private final UserValidate userValidate;
    private final PlatformRepository platformRepository;

    @Override
    @Transactional
    public void createPlatform(HttpServletRequest request, PlatformCreateRequestDto dto) {
        userValidate.invalidUserRole(request);

        try {
            PlatformEntity newPlatform = dto.toEntity();
            platformRepository.save(newPlatform);
        } catch (RuntimeException e) {
            throw new DuplicateException("이미 존재하거나 잘못된 플랫폼 이름입니다.", ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }

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

    @Override
    public void deletePlatform(HttpServletRequest request, Long id) {
        userValidate.validateUserRole(request);

        PlatformEntity platform = platformRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("존재하지 않는 플랫폼입니다.", ErrorCode.INTERNAL_SERVER_EXCEPTION));

        platformRepository.delete(platform);
    }



}
