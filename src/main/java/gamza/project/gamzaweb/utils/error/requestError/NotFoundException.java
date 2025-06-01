package gamza.project.gamzaweb.utils.error.requestError;
import gamza.project.gamzaweb.utils.error.ErrorCode;

public class NotFoundException extends BusinessException {

    public NotFoundException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}