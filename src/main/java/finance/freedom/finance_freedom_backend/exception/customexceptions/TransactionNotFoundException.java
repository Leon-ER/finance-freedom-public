package finance.freedom.finance_freedom_backend.exception.customexceptions;

public class TransactionNotFoundException extends RuntimeException {
    public TransactionNotFoundException(String message) {
        super(message);
    }
}
