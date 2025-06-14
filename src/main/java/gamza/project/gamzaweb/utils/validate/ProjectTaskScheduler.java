package gamza.project.gamzaweb.utils.validate;

import gamza.project.gamzaweb.dto.project.request.DeploymentUpdateRequest;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProjectTaskScheduler {

    private final DeploymentStepQueue deploymentStepQueue;
    private final ProjectRepository projectRepository;

    @Scheduled(fixedRate = 5000)
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

    }



}
