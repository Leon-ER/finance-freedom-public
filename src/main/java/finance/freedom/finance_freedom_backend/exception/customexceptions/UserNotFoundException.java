package finance.freedom.finance_freedom_backend.exception.customexceptions;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
