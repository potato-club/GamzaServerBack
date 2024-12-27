/*
package gamza.project.gamzaweb.Service.Impl;

import gamza.project.gamzaweb.Entity.ContainerEntity;
import gamza.project.gamzaweb.Service.Interface.DockerProviderContainerCallback;

public class DockerProviderContainerCallbackImpl implements DockerProviderContainerCallback {

    @Override
    public void onContainerCreated(String containerId) {
        // 컨테이너 생성 후 처리할 작업
        System.out.println("Container has been successfully created with ID: " + containerId);

        // 예: 컨테이너 ID를 데이터베이스에 저장
        saveContainerInfoToDatabase(containerId);
    }

    private void saveContainerInfoToDatabase(String containerId) {
        // DB 저장 로직 구현
        // 예: 컨테이너 정보를 저장하는 엔티티 및 리포지토리 사용
        ContainerEntity containerEntity = ContainerEntity.builder()
                .containerId(containerId)
                .status("Running")
                .createdAt(LocalDateTime.now())
                .build();
        containerRepository.save(containerEntity);

        System.out.println("Container info saved to database: " + containerId);
    }
}
*/
