package gamza.project.gamzaweb.error.requestError;
import gamza.project.gamzaweb.error.ErrorJwtCode;

public class JwtException extends JwsException {

    public JwtException(String message, ErrorJwtCode errorCode) {
        super(message, errorCode);
    }

}