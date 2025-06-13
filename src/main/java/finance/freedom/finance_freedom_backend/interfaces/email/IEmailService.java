package finance.freedom.finance_freedom_backend.interfaces.email;

import finance.freedom.finance_freedom_backend.dto.email.EmailVerificationTokenDTO;
import finance.freedom.finance_freedom_backend.enums.TokenPurpose;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.email.EmailVerificationToken;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import jakarta.mail.MessagingException;

public interface IEmailService {
    void sendEmail(String to, String subject, String text) throws MessagingException;

    EmailVerificationTokenDTO generateEmailVerificationToken(User user, TokenPurpose purpose);

    GenericResponse verifyToken(String token, TokenPurpose purpose, String newPassword, String oldPassword);

    String resendVerificationToken(String email) throws MessagingException;

    String emailContent(String token);

    String emailContentDeleteAccount(String token);

    String emailContentChangePassword(String token);

    String emailContentResetPassword(String token);
}
