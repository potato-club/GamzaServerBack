package gamza.project.gamzaweb.utils.error.requestError;
import gamza.project.gamzaweb.utils.error.ErrorCode;

public class UnAuthorizedException  extends BusinessException{

    public UnAuthorizedException(String message, ErrorCode code) {
        super(message, code);
    }
}
