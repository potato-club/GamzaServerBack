package gamza.project.gamzaweb.dctutil;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.model.Container;
import com.github.dockerjava.api.model.Image;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Component
public class DockerScheduler {
    //10초마다 반복하며 container 와 image를 체크하는 class

    private final DockerClient client = DockerDataStore.getInstance().getDockerClient();
//    private final


    @Scheduled(fixedRate = 10 * 1000)
    private void dockerScheduler() {
        List<Container> dockerContainers = client.listContainersCmd().withShowAll(true).exec();
        List<Image> dockerImages = client.listImagesCmd().withShowAll(true).exec();

        final Map<String, Container> dockerContainerMap = new HashMap<>();
        dockerContainers.forEach(Container -> dockerContainerMap.put(Container.getId(), Container));

        checkContainerMap.keySet().forEach(id -> {
            //todo : input db
            if (dockerContainerMap.containsKey(id)) {
                checkContainerMap.get(id).containerCheckResult(true, dockerContainerMap.get(id));
            } else {
                checkContainerMap.get(id).containerCheckResult(false, null);
            }
        });

        final Map<String, Image> dockerImageMap = new HashMap<>();
        dockerImages.forEach(image -> dockerImageMap.put(image.getId(), image));

        checkImageMap.keySet().forEach(id -> {
            //todo : input db
            if (dockerImageMap.containsKey(id)) {
                checkImageMap.get(id).containerCheckResult(true, dockerImageMap.get(id));
            } else {
                checkImageMap.get(id).containerCheckResult(false, null);
            }
        });

    }

    private boolean areAllIdsPresent(List<Image> images, List<String> ids) {
        // `images`의 `id` 값을 `Set`으로 수집
        Set<String> imageIds = images.stream()
                .map(Image::getId)
                .collect(Collectors.toSet());

        // `ids`에 있는 모든 `id`가 `imageIds`에 존재하는지 확인
        return imageIds.containsAll(ids);
    }

    private final Map<String, ContainContainerCallBack> checkContainerMap = new HashMap<>();
    private final Map<String, ContainImageCallBack> checkImageMap = new HashMap<>();

    public void addContainerCheckList(String containerId, ContainContainerCallBack containCallBack) {
        checkContainerMap.put(containerId, containCallBack);
    }

    public void addImageCheckList(String imageId, ContainImageCallBack containCallBack) {
        checkImageMap.put(imageId, containCallBack);
    }

    public void removeContainerCheckList(String containerId) {
        checkContainerMap.remove(containerId);
    }

    public void removeImageCheckList(String imageId) {
        checkImageMap.remove(imageId);
    }

    public interface ContainContainerCallBack {
        void containerCheckResult(boolean result, Container container);
    }

    // Image 관련 콜백 인터페이스
    public interface ContainImageCallBack {
        void containerCheckResult(boolean result, Image image);
    }
}
