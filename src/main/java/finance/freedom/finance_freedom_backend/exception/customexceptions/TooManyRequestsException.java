package finance.freedom.finance_freedom_backend.exception.customexceptions;

public class TooManyRequestsException extends RuntimeException {
    public TooManyRequestsException(String message) {
        super(message);
    }
}
