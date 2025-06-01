package gamza.project.gamzaweb.dto.project.request;

import gamza.project.gamzaweb.Entity.UserEntity;
import lombok.Data;

@Data
public class ImageBuildEventRequestDto {
    private UserEntity userPk;
    private String imageId;
    private String name;
    private String key;

    public ImageBuildEventRequestDto(UserEntity userPk, String imageId, String name, String key) {
        this.userPk = userPk;
        this.imageId = imageId;
        this.name = name;
        this.key = key;
    }
}
