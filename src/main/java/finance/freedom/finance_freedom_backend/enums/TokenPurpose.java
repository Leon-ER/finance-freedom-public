package finance.freedom.finance_freedom_backend.enums;

import lombok.Getter;

@Getter
public enum TokenPurpose {
    EMAIL_VERIFICATION("Email verified successfully"),
    ACCOUNT_DELETION("Account deleted successfully"),
    PASSWORD_CHANGE("Password changed successfully"),
    PASSWORD_RESET("Password changed successfully");

    private final String successMessage;

    TokenPurpose(String successMessage) {
        this.successMessage = successMessage;
    }
}
