package gamza.project.gamzaweb.utils.error.requestError;
import gamza.project.gamzaweb.utils.error.ErrorJwtCode;

public class JwtException extends JwsException {

    public JwtException(String message, ErrorJwtCode errorCode) {
        super(message, errorCode);
    }

}