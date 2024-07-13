package gamza.project.gamzaweb.Error.requestError;
import gamza.project.gamzaweb.Error.ErrorCode;

public class JwtException extends BusinessException {

    public JwtException(String message, ErrorCode errorCode) {
        super(message, errorCode);
    }

}