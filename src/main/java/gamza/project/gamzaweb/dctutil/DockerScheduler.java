//package gamza.project.gamzaweb.dctutil;
//
//import com.github.dockerjava.api.DockerClient;
//import com.github.dockerjava.api.command.BuildImageCmd;
//import com.github.dockerjava.api.command.BuildImageResultCallback;
//import com.github.dockerjava.api.model.BuildResponseItem;
//import com.github.dockerjava.api.model.Container;
//import com.github.dockerjava.api.model.Image;
//import gamza.project.gamzaweb.Entity.ImageEntity;
//import gamza.project.gamzaweb.Entity.UserEntity;
//import gamza.project.gamzaweb.Repository.ImageRepository;
//import io.micrometer.common.lang.Nullable;
//import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
//
//import java.util.*;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.Executors;
//import java.util.stream.Collectors;
//
//@Component
//public class DockerScheduler {
//    //10초마다 반복하며 container 와 image를 체크하는 class
//
//    private final DockerClient client = DockerDataStore.getInstance().getDockerClient();
//    private final ImageRepository imageRepository;
//
//    public DockerScheduler(ImageRepository imageRepository) {
//        this.imageRepository = imageRepository;
//    }
////    private final
//
//
//    @Scheduled(fixedRate = 10 * 1000)
//    private void dockerScheduler() {
//        List<Container> dockerContainers = client.listContainersCmd().withShowAll(true).exec();
//        List<Image> dockerImages = client.listImagesCmd().withShowAll(true).exec();
//
//        final Map<String, Container> dockerContainerMap = new HashMap<>();
//        dockerContainers.forEach(Container -> dockerContainerMap.put(Container.getId(), Container));
//
//        checkContainerMap.keySet().forEach(id -> {
//            //todo : input db
//            if (dockerContainerMap.containsKey(id)) {
//                checkContainerMap.get(id).containerCheckResult(true, dockerContainerMap.get(id));
//            } else {
//                checkContainerMap.get(id).containerCheckResult(false, null);
//            }
//        });
//
//        final Map<String, Image> dockerImageMap = new HashMap<>();
//        dockerImages.forEach(image -> dockerImageMap.put(image.getId(), image));
//
//        checkImageMap.keySet().forEach(id -> {
//            //todo : input db
//            if (dockerImageMap.containsKey(id)) {
//                checkImageMap.get(id).containerCheckResult(true, dockerImageMap.get(id));
//            } else {
//                checkImageMap.get(id).containerCheckResult(false, null);
//            }
//        });
//
//    }
//
//    @Scheduled(fixedRate = 10 * 1000)
//    private void dockerImageScheduler() {
//        List<Image> dockerImages = client.listImagesCmd().withShowAll(true).exec();
//
//        final Map<String, Image> dockerImageMap = new HashMap<>();
//        dockerImages.forEach(image -> dockerImageMap.put(image.getId(), image));
//
//        checkImageMap.keySet().forEach(id -> {
//            if (dockerImageMap.containsKey(id)) {
//                Image dockerImage = dockerImageMap.get(id);
//
//                // DB에 존재하는지 확인
//                Optional<ImageEntity> existingImage = imageRepository.findByImageId(id);
//                if (existingImage.isEmpty()) {
//                    // 존재하지 않으면, ImageEntity 생성 및 저장
//                    String name = dockerImage.getRepoTags() != null ? dockerImage.getRepoTags()[0] : null;
//
//                    saveImageEntity(id, name);
//                }
//
//                checkImageMap.get(id).containerCheckResult(true, dockerImage);
//            } else {
//                checkImageMap.get(id).containerCheckResult(false, null);
//            }
//        });
//    }
//
//    private void saveImageEntity(String imageId, String imageName) {
//        ImageEntity imageEntity = ImageEntity.builder()
//                .imageId(imageId)
//                .name(imageName)
//                .build();
//
//        imageRepository.save(imageEntity);
//    }
//
//    private boolean areAllIdsPresent(List<Image> images, List<String> ids) {
//        // `images`의 `id` 값을 `Set`으로 수집
//        Set<String> imageIds = images.stream()
//                .map(Image::getId)
//                .collect(Collectors.toSet());
//
//        // `ids`에 있는 모든 `id`가 `imageIds`에 존재하는지 확인
//        return imageIds.containsAll(ids);
//    }
//
//    private final Map<String, ContainContainerCallBack> checkContainerMap = new HashMap<>();
//    private final Map<String, ContainImageCallBack> checkImageMap = new HashMap<>();
//
//    public void addContainerCheckList(String containerId, ContainContainerCallBack containCallBack) {
//        checkContainerMap.put(containerId, containCallBack);
//    }
//
//    public void addImageCheckList(String imageId, ContainImageCallBack containCallBack) {
//        checkImageMap.put(imageId, containCallBack);
//    }
//
//    public void removeContainerCheckList(String containerId) {
//        checkContainerMap.remove(containerId);
//    }
//
//    public void removeImageCheckList(String imageId) {
//        checkImageMap.remove(imageId);
//    }
//
//    public interface ContainContainerCallBack {
//        void containerCheckResult(boolean result, Container container);
//    }
//
//    // Image 관련 콜백 인터페이스
//    public interface ContainImageCallBack {
//        void containerCheckResult(boolean result, Image image);
//    }
//
//
//}
