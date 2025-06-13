package finance.freedom.finance_freedom_backend.exception.customexceptions;

public class LinkedAccountNotFoundException extends RuntimeException {
    public LinkedAccountNotFoundException(String message) {
        super(message);
    }
}
