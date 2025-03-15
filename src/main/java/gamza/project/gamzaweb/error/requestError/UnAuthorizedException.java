package gamza.project.gamzaweb.error.requestError;
import gamza.project.gamzaweb.error.ErrorCode;

public class UnAuthorizedException  extends BusinessException{

    public UnAuthorizedException(String message, ErrorCode code) {
        super(message, code);
    }
}
