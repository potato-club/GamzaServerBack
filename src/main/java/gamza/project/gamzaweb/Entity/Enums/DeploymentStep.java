package gamza.project.gamzaweb.Entity.Enums;

import lombok.Getter;

@Getter
public enum DeploymentStep {
    INIT("STEP 1: 배포 시작"),
    ZIP_PATH_CHECK("STEP 2: 프로젝트 ZIP 경로 확인"),
    DOCKERFILE_EXTRACT("STEP 3: Dockerfile 추출 시작"),
    DOCKER_BUILD("STEP 4: Docker 이미지 빌드"),
    NGINX_CONFIG("STEP 5: Nginx 설정 생성"),
    NGINX_RELOAD("STEP 6: Nginx 재시작"),
    SUCCESS("STEP 7: 배포 완료"),
    FAILED("배포 실패");



    private final String description;

    DeploymentStep(String description) {
        this.description = description;
    }
}
