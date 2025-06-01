package gamza.project.gamzaweb.utils.error.requestError;

import gamza.project.gamzaweb.utils.error.ErrorJwtCode;
import lombok.Getter;

@Getter
public class JwsException extends RuntimeException {

    private final ErrorJwtCode errorCode;

    public JwsException(String message, ErrorJwtCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}
