package gamza.project.gamzaweb.Dto.platform;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlatformResponseDto {

    private Long platformId;
    private String platformName;
}
