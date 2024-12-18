package gamza.project.gamzaweb.Error.requestError;


import gamza.project.gamzaweb.Error.ErrorCode;
import lombok.Getter;

import java.util.NoSuchElementException;

@Getter
public class BusinessException extends RuntimeException{

    private final ErrorCode errorCode;

    public BusinessException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}