package gamza.project.gamzaweb.service.impl;

import gamza.project.gamzaweb.dto.platform.request.PlatformCreateRequestDto;
import gamza.project.gamzaweb.dto.platform.response.PlatformListResponseDto;
import gamza.project.gamzaweb.dto.platform.response.PlatformResponseDto;
import gamza.project.gamzaweb.Entity.PlatformEntity;
import gamza.project.gamzaweb.utils.error.ErrorCode;
import gamza.project.gamzaweb.utils.error.requestError.BadRequestException;
import gamza.project.gamzaweb.utils.error.requestError.DuplicateException;
import gamza.project.gamzaweb.repository.PlatformRepository;
import gamza.project.gamzaweb.service.Interface.PlatformService;
import gamza.project.gamzaweb.utils.validate.UserValidate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PlatformServiceImpl implements PlatformService {

    private final PlatformRepository platformRepository;

    @Override
    @Transactional
    public void createPlatform(PlatformCreateRequestDto dto) {

        try {
            PlatformEntity newPlatform = dto.toEntity();

            if (dto.toEntity().getPlatformName().isEmpty()) {
                throw new BadRequestException("플랫폼이름은 빈값이 될 수 없습니다.", ErrorCode.INTERNAL_SERVER_EXCEPTION);
            }

            platformRepository.save(newPlatform);
        } catch (RuntimeException e) {
            throw new DuplicateException("이미 존재하거나 잘못된 플랫폼 이름입니다.", ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }

    }

    @Override
    public PlatformListResponseDto getAllPlatformList() {

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
    public void deletePlatform(Long id) {

        PlatformEntity platform = platformRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("존재하지 않는 플랫폼입니다.", ErrorCode.INTERNAL_SERVER_EXCEPTION));

        platformRepository.delete(platform);
    }



}
