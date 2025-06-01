package gamza.project.gamzaweb.dto.project.response;

import gamza.project.gamzaweb.Entity.ApplicationEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationDetailResponseDto {
    private Long id;
    private String file;
    private int port;
    private String tag;
    private String variableKey;


    public ApplicationDetailResponseDto(ApplicationEntity application, String fileUrl) {
        this.id = application.getId();
        this.file = fileUrl; // 이미지 url 로 수정 s3 겟
        this.port = application.getOuterPort();
        this.tag = application.getTag();
        this.variableKey = application.getVariableKey();
    }
}
