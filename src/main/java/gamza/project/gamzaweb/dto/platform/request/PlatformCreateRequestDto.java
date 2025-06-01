package gamza.project.gamzaweb.dto.platform.request;

import gamza.project.gamzaweb.Entity.PlatformEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformCreateRequestDto {

    private String platformName;

    @Builder
    public PlatformEntity toEntity() {
        return PlatformEntity.builder()
                .platformName(platformName)
                .build();
    }

}
