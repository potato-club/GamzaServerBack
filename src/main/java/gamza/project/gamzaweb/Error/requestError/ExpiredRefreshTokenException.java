package gamza.project.gamzaweb.Error.requestError;
import gamza.project.gamzaweb.Error.ErrorCode;

public class ExpiredRefreshTokenException extends BusinessException {

    public ExpiredRefreshTokenException(String message, ErrorCode code) {
        super(message, code);
    }
}