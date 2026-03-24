package co.com.bancolombia.model.exception;

public class UnauthorizedException extends DomainException {

    public UnauthorizedException(String errorCode, String message) {
        super(errorCode, message);
    }
}
