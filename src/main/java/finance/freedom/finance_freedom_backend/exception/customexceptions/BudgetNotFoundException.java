package finance.freedom.finance_freedom_backend.exception.customexceptions;

public class BudgetNotFoundException extends RuntimeException {
    public BudgetNotFoundException(String message) {
        super(message);
    }
}
