package gamza.project.gamzaweb.Error.requestError;
import gamza.project.gamzaweb.Error.ErrorCode;

public class NotFoundException extends BusinessException {

    public NotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}