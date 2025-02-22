package gamza.project.gamzaweb.Controller;

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
    private static final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();
    private final Map<Long, String> deploymentStepCache = new ConcurrentHashMap<>();

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
        // 배포 상태 캐시에서 최신 상태 가져오기
        String lastStep = deploymentStepCache.getOrDefault(projectId, "배포 상태 없음");
        try {
            emitter.send(SseEmitter.event().name("deployment-step").data(lastStep));
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

        // 연결된 클라이언트에게 상태 전송
        for (SseEmitter emitter : new ArrayList<>(emitters.get(projectId))) {
            try {
                emitter.send(SseEmitter.event().name("deployment-step").data(step));
            } catch (IOException e) {
                emitter.complete();
                emitters.get(projectId).remove(emitter);
                System.out.println(" 클라이언트 연결 끊김, Emitter 제거됨");
            }
        }
    }
}
