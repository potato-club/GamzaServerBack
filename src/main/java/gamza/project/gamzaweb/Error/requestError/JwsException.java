package gamza.project.gamzaweb.Error.requestError;

import gamza.project.gamzaweb.Error.ErrorJwtCode;
import lombok.Getter;

@Getter
public class JwsException extends RuntimeException {

    private final ErrorJwtCode errorCode;

    public JwsException(String message, ErrorJwtCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}
