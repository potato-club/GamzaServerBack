package gamza.project.gamzaweb.error.requestError;
import gamza.project.gamzaweb.error.ErrorCode;

public class NotFoundException extends BusinessException {

    public NotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}