package gamza.project.gamzaweb.error.requestError;

import gamza.project.gamzaweb.error.ErrorCode;

public class DockerRequestException extends BusinessException {
    public DockerRequestException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}
