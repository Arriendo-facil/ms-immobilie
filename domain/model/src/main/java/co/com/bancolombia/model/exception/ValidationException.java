package co.com.bancolombia.model.exception;

public class ValidationException extends DomainException {

    public ValidationException(String errorCode, String message) {
        super(errorCode, message);
    }
}
