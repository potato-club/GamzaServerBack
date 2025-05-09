package gamza.project.gamzaweb.Entity.Enums;

import lombok.Getter;

@Getter
public enum DeploymentStep {
    NONE("배포 상태 없음"),
    INIT("1: 배포 시작"),
    ZIP_PATH_CHECK("2: 프로젝트 ZIP 경로 확인"),
    DOCKERFILE_EXTRACT("3: Dockerfile 추출 시작"),
    DOCKER_BUILD("4: Docker 이미지 빌드"),
    NGINX_CONFIG("5: Nginx 설정 생성"),
    SUCCESS("6: 배포 완료"),
    FAILED("배포 실패");

    private final String description;

    DeploymentStep(String description) {
        this.description = description;
    }
}
