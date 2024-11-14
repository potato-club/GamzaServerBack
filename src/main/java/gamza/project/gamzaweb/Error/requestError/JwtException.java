package gamza.project.gamzaweb.Error.requestError;
import gamza.project.gamzaweb.Error.ErrorJwtCode;

public class JwtException extends JwsException {

    public JwtException(String message, ErrorJwtCode errorCode) {
        super(message, errorCode);
    }

}