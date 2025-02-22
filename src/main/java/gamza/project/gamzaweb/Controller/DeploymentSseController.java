package gamza.project.gamzaweb.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gamza.project.gamzaweb.Dto.project.DeployStepResponseDto;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.Error.ErrorCode;
import gamza.project.gamzaweb.Error.requestError.ForbiddenException;
import gamza.project.gamzaweb.Repository.ProjectRepository;
import java.util.ArrayList;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/project/deploy")
public class DeploymentSseController {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final Map<Long, String> deploymentStepCache = new ConcurrentHashMap<>();
    private final ProjectRepository projectRepository;

    public DeploymentSseController(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    @GetMapping("/subscribe/{projectId}")
    public SseEmitter subscribe(@PathVariable Long projectId) {
        SseEmitter emitter = new SseEmitter(0L); // 무제한 타임아웃 설정
        emitters.computeIfAbsent(projectId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // 기존 배포 상태 즉시 전송
        sendLastDeploymentStep(projectId, emitter); // 여기에서 호출됨

        emitter.onCompletion(() -> emitters.get(projectId).remove(emitter));
        emitter.onTimeout(() -> emitters.get(projectId).remove(emitter));
        emitter.onError((e) -> emitters.get(projectId).remove(emitter));

        return emitter;
    }

    private void sendLastDeploymentStep(Long projectId, SseEmitter emitter) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ForbiddenException("프로젝트를 찾을 수 없습니다.", ErrorCode.FAILED_PROJECT_ERROR));
        String lastStep = deploymentStepCache.getOrDefault(projectId, project.getDeploymentStep());

        try {
            // DTO 객체 생성 후 JSON 변환
            String jsonData = objectMapper.writeValueAsString(new DeployStepResponseDto(lastStep));

            // event 없이 순수 JSON 데이터만 전송
            emitter.send(jsonData);
        } catch (IOException e) {
            emitter.complete();
            emitters.get(projectId).remove(emitter);
        }
    }

    public void sendUpdate(Long projectId, String step) {
        deploymentStepCache.put(projectId, step); // 최신 배포 상태 저장

        if (!emitters.containsKey(projectId)) {
            return;
        }

        // JSON 변환
        String jsonData;
        try {
            jsonData = objectMapper.writeValueAsString(new DeployStepResponseDto(step));
        } catch (IOException e) {
            System.out.println("JSON 변환 오류: " + e.getMessage());
            return;
        }

        // 연결된 클라이언트에게 JSON만 전송
        for (SseEmitter emitter : new ArrayList<>(emitters.get(projectId))) {
            try {
                emitter.send(jsonData); // DTO를 사용해 JSON 전송
            } catch (IOException e) {
                emitter.complete();
                emitters.get(projectId).remove(emitter);
                System.out.println("클라이언트 연결 끊김, Emitter 제거됨");
            }
        }
    }
}
