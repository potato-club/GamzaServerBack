package gamza.project.gamzaweb.Error.requestError;
import gamza.project.gamzaweb.Error.ErrorCode;

public class UnAuthorizedException  extends BusinessException{

    public UnAuthorizedException(String message, ErrorCode code) {
        super(message, code);
    }
}
