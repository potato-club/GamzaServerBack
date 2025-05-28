package gamza.project.gamzaweb.utils;

import gamza.project.gamzaweb.Entity.ProjectEntity;
import gamza.project.gamzaweb.Entity.UserEntity;
import gamza.project.gamzaweb.error.ErrorCode;
import gamza.project.gamzaweb.error.requestError.NotFoundException;
import gamza.project.gamzaweb.error.requestError.UnAuthorizedException;
import gamza.project.gamzaweb.repository.ProjectRepository;
import gamza.project.gamzaweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JpaAssistance {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public UserEntity getUserPkValue(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UnAuthorizedException("잘못된 요청입니다 - 해당 유저가 존재하지 않습니다.", ErrorCode.BAD_REQUEST_EXCEPTION));
    }

    public ProjectEntity getProjectPkValue(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("잘못된 요청입니다 - 해당 유저가 존재하지 않습니다.", ErrorCode.BAD_REQUEST_EXCEPTION));
    }

}
