package gamza.project.gamzaweb.error.requestError;

import gamza.project.gamzaweb.error.ErrorJwtCode;
import lombok.Getter;

@Getter
public class JwsException extends RuntimeException {

    private final ErrorJwtCode errorCode;

    public JwsException(String message, ErrorJwtCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

}
