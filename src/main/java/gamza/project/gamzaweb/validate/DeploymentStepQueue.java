package gamza.project.gamzaweb.validate;

import gamza.project.gamzaweb.dto.project.request.DeploymentUpdateRequest;
import gamza.project.gamzaweb.Entity.Enums.DeploymentStep;
import gamza.project.gamzaweb.Entity.ProjectEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@RequiredArgsConstructor
public class DeploymentStepQueue {

    private final Queue<DeploymentUpdateRequest> updateQueue = new ConcurrentLinkedQueue<>(); //
//    private final Queue<RequestApprovalProjectStatus> updateProjectQueue = new ConcurrentLinkedQueue<>(); //

    public void addDeploymentUpdate(ProjectEntity project, DeploymentStep step) {
        updateQueue.add(new DeploymentUpdateRequest(project.getId(), step));
    }

//    public void addApprovalProjectStatusUpdate(ProjectEntity project, ApprovalProjectStatus status) {
//        updateProjectQueue.add(new RequestApprovalProjectStatus(project.getId(), status));
//    }

    public List<DeploymentUpdateRequest> pollQueue() {
        List<DeploymentUpdateRequest> updates = new ArrayList<>();
        while (!updateQueue.isEmpty()) {
            updates.add(updateQueue.poll());
        }
        return updates;
    }
}
