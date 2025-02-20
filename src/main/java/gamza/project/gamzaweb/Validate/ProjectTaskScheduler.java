package gamza.project.gamzaweb.Validate;

import gamza.project.gamzaweb.Dto.project.DeploymentUpdateRequest;
import gamza.project.gamzaweb.Entity.Enums.DeploymentStep;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.Repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@RequiredArgsConstructor
public class ProjectTaskScheduler {

    private final DeploymentStepQueue deploymentStepQueue;
    private final ProjectRepository projectRepository;

    @Scheduled(fixedRate = 5000) // 5sec
    public void processDeploymentSteps() {
        List<DeploymentUpdateRequest> updateRequests = deploymentStepQueue.pollQueue();
        if(updateRequests.isEmpty()) {
            return;
        }

        updateRequests.forEach(update -> {
                Optional<ProjectEntity> projectOpt = projectRepository.findById(update.getId());
                projectOpt.ifPresent(project -> {
                    project.updateDeploymentStep(update.getStep().getDescription());
                    projectRepository.save(project);
                });
        });

        System.out.println("여기까지 뜨면 다된거긴해 제발용~꼬리용용");
    }



}
