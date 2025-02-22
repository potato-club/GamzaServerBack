package gamza.project.gamzaweb.Controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@RestController
@RequestMapping("/project/deploy")
public class DeploymentSseController {
    private static final ConcurrentHashMap<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    @GetMapping("/subscribe/{projectId}")
    public SseEmitter subscribe(@PathVariable Long projectId) {
        SseEmitter emitter = new SseEmitter(0L); // 🔥 타임아웃 무제한 설정
        emitters.computeIfAbsent(projectId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        // 🔥 5초마다 Heartbeat(핑) 메시지 전송 (연결 유지)
        new Thread(() -> {
            while (true) {
                try {
                    emitter.send(SseEmitter.event().name("ping").data("heartbeat"));
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

    public void sendUpdate(Long projectId, String step) {
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
