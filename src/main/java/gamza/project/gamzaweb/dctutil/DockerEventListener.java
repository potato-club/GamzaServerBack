package gamza.project.gamzaweb.dctutil;

import gamza.project.gamzaweb.Entity.ImageEntity;
import gamza.project.gamzaweb.dto.project.request.ImageBuildEventRequestDto;
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
    public void handleImageBuildEvent(ImageBuildEventRequestDto event) {
        Optional<ImageEntity> optionalImageEntity = imageRepository.findByNameAndUser(event.getName(), event.getUserPk());
        if (optionalImageEntity.isPresent()) {
            ImageEntity imageEntity = optionalImageEntity.get();

            imageEntity.updatedImageId(event.getImageId());
            imageRepository.save(imageEntity);
        }
    }

}
