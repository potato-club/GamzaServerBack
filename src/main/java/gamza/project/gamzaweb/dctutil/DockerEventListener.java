package gamza.project.gamzaweb.dctutil;

import gamza.project.gamzaweb.dto.docker.ImageBuildEventDto;
import gamza.project.gamzaweb.Entity.ImageEntity;
import gamza.project.gamzaweb.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class DockerEventListener {

    private final ImageRepository imageRepository;

    @EventListener
    public void handleImageBuildEvent(ImageBuildEventDto event) {
        Optional<ImageEntity> optionalImageEntity = imageRepository.findByNameAndUser(event.getName(), event.getUserPk());
        if (optionalImageEntity.isPresent()) {
            ImageEntity imageEntity = optionalImageEntity.get(); // 실제 ImageEntity 객체를 가져옴

            imageEntity.updatedImageId(event.getImageId());
            imageRepository.save(imageEntity);
//            ImageEntity updatedImageEntity = ImageEntity.builder()
//                    .user(imageEntity.getUser())
//                    .imageId(event.getImageId()) // 새로 업데이트된 imageId
//                    .name(imageEntity.getName())
//                    .variableKey(imageEntity.getVariableKey())
//                    .build();
//            imageRepository.save(updatedImageEntity); // 업데이트된 엔티티 저장
        }
    }

}
