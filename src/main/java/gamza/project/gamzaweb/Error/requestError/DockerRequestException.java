package gamza.project.gamzaweb.Error.requestError;

import gamza.project.gamzaweb.Error.ErrorCode;

public class DockerRequestException extends BusinessException {
    public DockerRequestException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}
