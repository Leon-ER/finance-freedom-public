package finance.freedom.finance_freedom_backend.exception.customexceptions;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
