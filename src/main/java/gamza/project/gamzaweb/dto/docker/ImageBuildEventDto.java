package gamza.project.gamzaweb.dto.docker;

import gamza.project.gamzaweb.Entity.UserEntity;
import lombok.Data;

@Data
public class ImageBuildEventDto {
    private UserEntity userPk;
    private String imageId;
    private String name;
    private String key;

    public ImageBuildEventDto(UserEntity userPk, String imageId, String name, String key) {
        this.userPk = userPk;
        this.imageId = imageId;
        this.name = name;
        this.key = key;
    }
}
