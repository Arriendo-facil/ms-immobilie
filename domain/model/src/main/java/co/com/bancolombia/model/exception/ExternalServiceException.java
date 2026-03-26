package co.com.bancolombia.model.exception;

public class ExternalServiceException extends DomainException {

    public ExternalServiceException(String errorCode, String message) {
        super(errorCode, message);
    }
}
