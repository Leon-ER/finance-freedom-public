package finance.freedom.finance_freedom_backend.service.core;

import finance.freedom.finance_freedom_backend.dto.email.EmailVerificationTokenDTO;
import finance.freedom.finance_freedom_backend.dto.user.RefreshTokenRequest;
import finance.freedom.finance_freedom_backend.dto.user.UserDetailsRequestDTO;
import finance.freedom.finance_freedom_backend.enums.TokenPurpose;
import finance.freedom.finance_freedom_backend.interfaces.aws.IAwsS3Service;
import finance.freedom.finance_freedom_backend.interfaces.core.IUserService;
import finance.freedom.finance_freedom_backend.interfaces.email.IEmailService;
import finance.freedom.finance_freedom_backend.interfaces.jwt.IJWTService;
import finance.freedom.finance_freedom_backend.model.core.RefreshToken;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.model.security.JWT;
import finance.freedom.finance_freedom_backend.repository.RefreshTokenRepository;
import finance.freedom.finance_freedom_backend.repository.UserRepository;
import finance.freedom.finance_freedom_backend.service.security.CustomUserDetailsService;
import finance.freedom.finance_freedom_backend.util.AuthorizationUtils;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements IUserService {

    private final UserRepository userRepository;

    private final CustomUserDetailsService customUserDetailsService;

    private final IJWTService jwtService;

    private final RefreshTokenRepository refreshTokenRepository;

    private final IAwsS3Service s3;

    private final IEmailService emailService;

    @Cacheable(value = "user", key = "#user.userId")
    @Override
    public UserDetailsRequestDTO getUserDetails(User user) {
        log.info("Attempting to get user details for user {}", user.getEmail());
        return new UserDetailsRequestDTO(user.getFullName(), user.getEmail());
    }

    @Override
    public GenericResponse deleteAccount(User user) throws MessagingException {
        log.info("Attempting to send account deletion email for user {}", user.getEmail());
        EmailVerificationTokenDTO token = emailService.generateEmailVerificationToken(user, TokenPurpose.ACCOUNT_DELETION);

        sendAccountDeletionEmail(user,token.getToken());

        log.info("Email sent successfully to {}", user.getEmail());

        return new GenericResponse("Account deletion email sent successfully");
    }

    @Override
    public JWT refresh(User user, RefreshTokenRequest refreshTokenRequest) {
        log.info("Attempting to refresh token");
        String token = refreshTokenRequest.getRefreshToken();
        String email = jwtService.extractUserName(token);

        RefreshToken refreshToken = refreshTokenRepository.findByRefreshToken(token);

        AuthorizationUtils.validateOwnership(refreshToken.getUser().getUserId(), user.getUserId());

        if (email == null) {
            log.warn("Token does not contain a valid email");
            throw new IllegalArgumentException("Token does not contain a valid email");
        }

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(user.getEmail());
        if (!jwtService.validateToken(token, userDetails)) {
            log.warn("Invalid refresh token");
            throw new IllegalArgumentException("Invalid refresh token");
        }
        log.info("Successfully refreshed access token for user: {}", email);
        return jwtService.generateToken(user, Duration.ofMinutes(15));
    }

    @Override
    public GenericResponse logout(User user, RefreshTokenRequest refreshTokenRequest) {
        log.info("Attempting to logout user");
        RefreshToken token = refreshTokenRepository.findByRefreshToken(refreshTokenRequest.getRefreshToken());

        if (token == null) {
            log.warn("Logout failed: refresh token not found");
            throw new IllegalArgumentException("Invalid refresh token");
        }

        AuthorizationUtils.validateOwnership(token.getUser().getUserId(), user.getUserId());

        refreshTokenRepository.delete(token);
        log.info("Logout successful");
        return new GenericResponse("Logout successful");
    }

    @Override
    public GenericResponse resetPassword(User user) throws MessagingException {
        log.info("Attempting to send password change email for user {}", user.getEmail());
        EmailVerificationTokenDTO token = emailService.generateEmailVerificationToken(user, TokenPurpose.PASSWORD_CHANGE);

        sendPasswordChangeEmail(user,token.getToken());

        log.info("Email sent successfully to {}", user.getEmail());

        return new GenericResponse("Password change email sent successfully");
    }

    public void sendAccountDeletionEmail(User user, String token) throws MessagingException {
        String subject = "Account deletion - Finance Freedom";

        String htmlMessage = emailService.emailContentDeleteAccount(token);

        emailService.sendEmail(user.getEmail(), subject, htmlMessage);
    }
    public void sendPasswordChangeEmail(User user, String token) throws MessagingException {
        String subject = "Password reset - Finance Freedom";

        String htmlMessage = emailService.emailContentChangePassword(token);

        emailService.sendEmail(user.getEmail(), subject, htmlMessage);
    }
}
