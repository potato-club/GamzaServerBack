package gamza.project.gamzaweb.Controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/project/deploy")
public class DeploymentSseController {
    private static final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    // 🔥 배포 상태 캐시 (각 프로젝트의 최신 배포 상태 저장)
    private final Map<Long, String> deploymentStepCache = new ConcurrentHashMap<>();

    @GetMapping("/subscribe/{projectId}")
    public SseEmitter subscribe(@PathVariable Long projectId) {
        SseEmitter emitter = new SseEmitter(0L); // 🔥 타임아웃 무제한 설정
        emitters.computeIfAbsent(projectId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // 🔥 기존 배포 상태 즉시 전송
        sendLastDeploymentStep(projectId, emitter);

        // 🔥 5초마다 배포 상태 전송 (프론트 요청 반영)
        new Thread(() -> {
            while (true) {
                try {
                    String lastStep = deploymentStepCache.getOrDefault(projectId, "🚀 배포 상태 없음");
                    emitter.send(SseEmitter.event().name("deployment-step").data(lastStep));
                    Thread.sleep(5000);
                } catch (Exception e) {
                    emitter.complete();
                    emitters.get(projectId).remove(emitter);
                    break;
                }
            }
        }).start();

        emitter.onCompletion(() -> emitters.get(projectId).remove(emitter));
        emitter.onTimeout(() -> emitters.get(projectId).remove(emitter));

        return emitter;
    }

    /**
     * 구독 시, 해당 프로젝트의 최신 배포 상태 전송
     */
    private void sendLastDeploymentStep(Long projectId, SseEmitter emitter) {
        String lastStep = deploymentStepCache.getOrDefault(projectId, "🚀 배포 상태 없음");
        try {
            emitter.send(SseEmitter.event().name("deployment-step").data(lastStep));
        } catch (IOException e) {
            emitter.complete();
            emitters.get(projectId).remove(emitter);
        }
    }

    /**
     * 배포 상태 업데이트 및 SSE 전송
     */
    public void sendUpdate(Long projectId, String step) {
        // 🔥 최신 배포 상태 캐시에 저장
        deploymentStepCache.put(projectId, step); // 👈 배포 상태 저장

        if (emitters.containsKey(projectId)) {
            for (SseEmitter emitter : emitters.get(projectId)) {
                try {
                    emitter.send(SseEmitter.event().name("deployment-step").data(step));
                } catch (IOException e) {
                    emitter.complete();
                    emitters.get(projectId).remove(emitter);
                }
            }
        }
    }
}
