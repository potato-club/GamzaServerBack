package gamza.project.gamzaweb.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gamza.project.gamzaweb.dto.project.response.DeployStepResponseDto;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.error.ErrorCode;
import gamza.project.gamzaweb.error.requestError.ForbiddenException;
import gamza.project.gamzaweb.repository.ProjectRepository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;

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
        SseEmitter emitter = new SseEmitter(10 * 60 * 1000L); // 10분 타임아웃 설정
        emitters.computeIfAbsent(projectId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // 기존 배포 상태 즉시 전송
        sendLastDeploymentStep(projectId, emitter);

        // 30초마다 ping 메시지 전송 (연결 유지)
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                emitter.send("ping: 연결 유지를 위한 핑\n\n");
            } catch (IOException e) {
                emitter.complete();
                removeEmitter(projectId, emitter);
                scheduler.shutdown();  // 예외 발생 시 스케줄러 종료
            }
        }, 30, 30, TimeUnit.SECONDS);

        // 클라이언트가 연결을 끊으면 정리
        emitter.onCompletion(() -> removeEmitter(projectId, emitter));
        emitter.onTimeout(() -> removeEmitter(projectId, emitter));
        emitter.onError((e) -> removeEmitter(projectId, emitter));

        return emitter;
    }

    private void sendLastDeploymentStep(Long projectId, SseEmitter emitter) {
        ProjectEntity project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ForbiddenException("프로젝트를 찾을 수 없습니다.", ErrorCode.FAILED_PROJECT_ERROR));
        String lastStep = deploymentStepCache.getOrDefault(projectId, project.getDeploymentStep());

        try {
            if (emitter != null) {
                String jsonData = objectMapper.writeValueAsString(new DeployStepResponseDto(lastStep));
                emitter.send(jsonData);
            }
        } catch (IOException | IllegalStateException e) {
            removeEmitter(projectId, emitter);
        }
    }

    private void removeEmitter(Long projectId, SseEmitter emitter) {
        List<SseEmitter> projectEmitters = emitters.get(projectId);
        if (projectEmitters != null) {
            projectEmitters.remove(emitter);
            if (projectEmitters.isEmpty()) {
                emitters.remove(projectId); // 클라이언트가 없으면 삭제
            }
        }
    }

    public void sendUpdate(Long projectId, String step) {
        deploymentStepCache.put(projectId, step); // 최신 배포 상태 저장

        if (!emitters.containsKey(projectId)) {
            return;
        }

        String jsonData;
        try {
            jsonData = objectMapper.writeValueAsString(new DeployStepResponseDto(step));
        } catch (IOException e) {
            System.out.println("JSON 변환 오류: " + e.getMessage());
            return;
        }

        List<SseEmitter> toRemove = new ArrayList<>();
        for (SseEmitter emitter : new ArrayList<>(emitters.get(projectId))) {
            try {
                emitter.send(jsonData);
            } catch (IOException | IllegalStateException e) { // 응답이 닫힌 경우 예외 처리
                emitter.complete();
                toRemove.add(emitter);
            }
        }

        // 응답이 종료된 Emitter를 한 번에 제거
        emitters.get(projectId).removeAll(toRemove);
        if (emitters.get(projectId).isEmpty()) {
            emitters.remove(projectId);
        }
    }
}
