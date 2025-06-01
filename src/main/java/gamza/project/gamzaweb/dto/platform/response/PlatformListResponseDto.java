package gamza.project.gamzaweb.dto.platform.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformListResponseDto {

    private List<PlatformResponseDto> platformResponseDtos;

}
