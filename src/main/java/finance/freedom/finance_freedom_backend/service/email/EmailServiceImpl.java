package finance.freedom.finance_freedom_backend.service.email;

import finance.freedom.finance_freedom_backend.dto.email.EmailVerificationTokenDTO;
import finance.freedom.finance_freedom_backend.enums.TokenPurpose;
import finance.freedom.finance_freedom_backend.interfaces.aws.IAwsS3Service;
import finance.freedom.finance_freedom_backend.interfaces.email.IEmailService;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.email.EmailVerificationToken;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.repository.EmailVerificationTokenRepository;
import finance.freedom.finance_freedom_backend.repository.RefreshTokenRepository;
import finance.freedom.finance_freedom_backend.repository.UserRepository;
import finance.freedom.finance_freedom_backend.util.PasswordUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender javaMailSender;

    private final UserRepository userRepository;

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final IAwsS3Service s3;

    private final PasswordUtil passwordUtil;



    @Override
    public void sendEmail(String to, String subject, String text) throws MessagingException {
        log.info("Sending email to {}", to);
        MimeMessage message = javaMailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text, true);

        javaMailSender.send(message);
        log.info("Email sent successfully");
    }

    @Override
    public EmailVerificationTokenDTO generateEmailVerificationToken(User user, TokenPurpose purpose) {
        log.info("Generating email verification token for user {}", user.getEmail());

        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setToken(java.util.UUID.randomUUID().toString());

        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        token.setPurpose(purpose);

        emailVerificationTokenRepository.save(token);

        log.info("Email verification token generated successfully");

        return createDTO(token);
    }

    @Override
    public GenericResponse verifyToken(String token, TokenPurpose purpose, String newPassword, String oldPassword) {
        log.info("Verifying token {}", token);
        EmailVerificationToken emailVerificationToken = emailVerificationTokenRepository.findByToken(token);

        if (emailVerificationToken == null) {
            log.warn("Invalid verification token");
            throw new IllegalArgumentException("Invalid verification token");
        }

        User user = emailVerificationToken.getUser();


        if (emailVerificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Verification token expired");
            throw new IllegalArgumentException("Verification token expired");
        }

        if (!emailVerificationToken.getPurpose().equals(purpose)) {
            log.warn("Token purpose mismatch: expected {}, got {}", purpose, emailVerificationToken.getPurpose());
            throw new IllegalArgumentException("Token does not match the expected purpose");
        }

        switch (purpose) {
            case EMAIL_VERIFICATION -> {
                user.setVerified(true);
                userRepository.save(user);
                s3.createFolder(user.getUserId());
                log.info("User verified successfully {}", user.getEmail());
            }
            case ACCOUNT_DELETION -> {
                userRepository.delete(user);
                s3.deleteFolder(user.getUserId());
                emailVerificationToken = null;
                log.info("User deleted successfully {}", user.getUserId());
            }
            case PASSWORD_CHANGE -> {
                if (newPassword == null || oldPassword == null) {
                    log.warn("Old or new password was null for password change operation for user {}", user.getEmail());
                    throw new IllegalArgumentException("New and old passwords must not be null for password change.");
                }

                if (!passwordUtil.verify(oldPassword, user.getPasswordHash())) {
                    log.warn("Old password didnt match current password in DB for user {}", user.getEmail());
                    throw new IllegalArgumentException("Old password is incorrect.");
                }

                user.setPasswordHash(passwordUtil.hash(newPassword));
                userRepository.save(user);
                refreshTokenRepository.deleteAllByUser(user);
                log.info("Password changed for {}", user.getEmail());
            }
            case PASSWORD_RESET -> {
                if(newPassword == null){
                    log.warn("New password was null for password reset");
                    throw new IllegalArgumentException("New password must not be null for password reset");
                }
                user.setPasswordHash(passwordUtil.hash(newPassword));
                userRepository.save(user);
                refreshTokenRepository.deleteAllByUser(user);
                log.info("Password reset for {}", user.getEmail());
            }
        }

        if(emailVerificationToken != null){
            emailVerificationTokenRepository.delete(emailVerificationToken);
        }

        log.info("Token verification for purpose {} completed for user {}", purpose, user.getEmail());
        return new GenericResponse(purpose.getSuccessMessage());
    }


    @Override
    public String resendVerificationToken(String email) throws MessagingException {
        log.info("Resending verification token for user {}", email);

        User user = userRepository.findByEmail(email);

        if (user == null) {
            log.warn("User not found");
            return "User not found";
        }

        if (user.isVerified()) {
            log.warn("User already verified");
            return "User already verified";
        }

        emailVerificationTokenRepository.deleteByUser(user);


        EmailVerificationTokenDTO tokenDTO = generateEmailVerificationToken(user, TokenPurpose.EMAIL_VERIFICATION);

        String subject = "Account Verification - Finance Freedom";


        sendEmail(user.getEmail(),subject,emailContent(tokenDTO.getToken()));

        log.info("Verification email sent successfully for user {}", email);

        return "Verification email has been resent successfully.";
    }

    public String emailContent(String token) {
        return String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif;'>" +
                        "<div style='background-color: #f5f5f5; padding: 20px;'>" +
                        "<h2 style='color: #333;'>Welcome to our app!</h2>" +
                        "<p style='font-size: 16px;'>Please verify your email by clicking the button below:</p>" +
                        "<form id='verifyForm' method='POST' action='http://localhost:8080/api/auth/verify'>" +
                        "<input type='hidden' name='token' value='%s'>" +
                        "<input type='hidden' name='purpose' value='EMAIL_VERIFICATION'>" +
                        "<button type='submit' style='padding: 10px 20px; background-color: #007bff; color: white; border: none; border-radius: 5px; cursor: pointer;'>Verify Email</button>" +
                        "</form>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                token
        );
    }


    @Override
    public String emailContentDeleteAccount(String token) {
        return String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif;'>" +
                        "<div style='background-color: #f5f5f5; padding: 20px;'>" +
                        "<h2 style='color: #d9534f;'>Confirm Account Deletion</h2>" +
                        "<p style='font-size: 16px;'>We're sorry to see you go. If you're sure about deleting your account, please confirm by clicking the button below:</p>" +
                        "<form method='POST' action='http://localhost:8080/api/user/verify'>" +
                        "<input type='hidden' name='token' value='%s'>" +
                        "<input type='hidden' name='purpose' value='ACCOUNT_DELETION'>" +
                        "<button type='submit' style='padding: 10px 20px; background-color: #dc3545; color: white; border: none; border-radius: 5px; cursor: pointer;'>Delete Account</button>" +
                        "</form>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                token
        );
    }

    @Override
    public String emailContentResetPassword(String token) {
        return String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif;'>" +
                        "<div style='background-color: #f5f5f5; padding: 20px;'>" +
                        "<h2 style='color: #333;'>Reset Your Password</h2>" +
                        "<p style='font-size: 16px;'>It looks like you requested a password reset. Click the button below to set a new password:</p>" +
                        "<form method='POST' action='http://localhost:8080/api/user/verify'>" +
                        "<input type='hidden' name='token' value='%s'>" +
                        "<input type='hidden' name='purpose' value='PASSWORD_RESET'>" +
                        "<button type='submit' style='padding: 10px 20px; background-color: #17a2b8; color: white; border: none; border-radius: 5px; cursor: pointer;'>Reset Password</button>" +
                        "</form>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                token
        );
    }

    @Override
    public String emailContentChangePassword(String token) {
        return String.format(
                "<html>" +
                        "<body style='font-family: Arial, sans-serif;'>" +
                        "<div style='background-color: #f5f5f5; padding: 20px;'>" +
                        "<h2 style='color: #333;'>Confirm Password Change</h2>" +
                        "<p style='font-size: 16px;'>You requested to change your password. Click the button below to confirm and proceed:</p>" +
                        "<form method='POST' action='http://localhost:8080/api/user/verify'>" +
                        "<input type='hidden' name='token' value='%s'>" +
                        "<input type='hidden' name='purpose' value='PASSWORD_CHANGE'>" +
                        "<button type='submit' style='padding: 10px 20px; background-color: #ffc107; color: white; border: none; border-radius: 5px; cursor: pointer;'>Change Password</button>" +
                        "</form>" +
                        "</div>" +
                        "</body>" +
                        "</html>",
                token
        );
    }


    public EmailVerificationTokenDTO createDTO(EmailVerificationToken token){
        EmailVerificationTokenDTO dto = new EmailVerificationTokenDTO();

        dto.setToken(token.getToken());
        dto.setPurpose(token.getPurpose());
        dto.setExpiresAt(token.getExpiresAt());

        return dto;
    }
}
