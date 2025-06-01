package gamza.project.gamzaweb.utils.error.requestError;

import gamza.project.gamzaweb.utils.error.ErrorCode;

public class DockerRequestException extends BusinessException {
    public DockerRequestException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}
