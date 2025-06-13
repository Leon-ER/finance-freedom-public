package finance.freedom.finance_freedom_backend.service.core;

import finance.freedom.finance_freedom_backend.dto.email.EmailVerificationTokenDTO;
import finance.freedom.finance_freedom_backend.dto.user.*;
import finance.freedom.finance_freedom_backend.enums.TokenPurpose;
import finance.freedom.finance_freedom_backend.exception.customexceptions.UserNotFoundException;
import finance.freedom.finance_freedom_backend.interfaces.core.IAuthService;
import finance.freedom.finance_freedom_backend.interfaces.email.IEmailService;
import finance.freedom.finance_freedom_backend.interfaces.jwt.IJWTService;
import finance.freedom.finance_freedom_backend.model.core.RefreshToken;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.model.security.CustomUserDetails;
import finance.freedom.finance_freedom_backend.model.security.JWT;
import finance.freedom.finance_freedom_backend.repository.RefreshTokenRepository;
import finance.freedom.finance_freedom_backend.repository.UserRepository;
import finance.freedom.finance_freedom_backend.util.PasswordUtil;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;

@RequiredArgsConstructor
@Service
@Slf4j
public class AuthServiceImpl implements IAuthService {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final IJWTService jwtService;

    private final RefreshTokenRepository refreshTokenRepository;

    private final IEmailService emailService;

    private final PasswordUtil passwordUtil;

    @Override
    public CreateUserResponseDTO save(CreateUserDTO userDTO) throws MessagingException {
        log.info("Attempting to save user {}", userDTO.getEmail());

        User user = new User();
        user.setPasswordHash(passwordUtil.hash(userDTO.getPassword()));
        user.setFullName(userDTO.getFullName());
        user.setEmail(userDTO.getEmail());
        user.setOauthProvider(userDTO.getOauthProvider());
        user.setOauthId(userDTO.getOauthId());
        user.setActive(true);
        user.setVerified(false);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);

        EmailVerificationTokenDTO token = emailService.generateEmailVerificationToken(user, TokenPurpose.EMAIL_VERIFICATION);

        sendVerificationEmail(user,token.getToken());

        log.info("User saved successfully {} and email verification sent", user.getEmail());

            return new CreateUserResponseDTO(
                    "User saved successfully",
                    user.getEmail(),
                    user.getFullName()
            );
    }

    @Override
    public AuthenticatedUserResponseDTO login(LoginRequestDTO loginRequestDTO) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequestDTO.getEmail(), loginRequestDTO.getPassword()));

        if(authentication.isAuthenticated()){
            log.info("Logged in user {}", authentication.getName());
            AuthenticatedUserResponseDTO authenticatedUserResponseDTO = new AuthenticatedUserResponseDTO();
            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            User user = customUserDetails.getUser();

            if(!user.isVerified()){
                log.warn("User {} is not verified", user.getEmail());
                throw new IllegalArgumentException("User is not verified");
            }

            if(loginRequestDTO.getRememberMe()){
               JWT refreshToken = jwtService.generateToken(user, Duration.ofDays(7));

               RefreshToken token = new RefreshToken();

               token.setRefreshToken(refreshToken.getToken());
               token.setUser(user);
               token.setCreatedAt(LocalDateTime.now());

                LocalDateTime expiresAt = refreshToken.getExpiresAt()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();

                token.setExpiresAt(expiresAt);

               refreshTokenRepository.save(token);

            }

            JWT accessToken = jwtService.generateToken(user, Duration.ofMinutes(15));

            user.setLastLoginAt(LocalDateTime.now());

            userRepository.save(user);

            authenticatedUserResponseDTO.setUserId(user.getUserId());
            authenticatedUserResponseDTO.setEmail(user.getEmail());
            authenticatedUserResponseDTO.setUsername(user.getEmail());
            authenticatedUserResponseDTO.setAccessToken(accessToken.getToken());
            authenticatedUserResponseDTO.setExpiresAt(accessToken.getExpiresAt());

            log.info("User {} logged in successfully", user.getEmail());

            return authenticatedUserResponseDTO;

        }
        return null;
    }

    @Override
    public GenericResponse passwordReset(String email) throws MessagingException {
        log.info("Attempting to send password reset email to {}", email);
        User user = userRepository.findByEmail(email);

        if(user == null){
            log.warn("Password reset requested for non-existent email: {}", email);
            throw new UserNotFoundException(String.format("User with email %s not found",email));
        }

        EmailVerificationTokenDTO token = emailService.generateEmailVerificationToken(user, TokenPurpose.PASSWORD_RESET);

        sendPasswordResetEmail(user,token.getToken());

        log.info("Password reset email sent successfully to {}", email);
        return new GenericResponse("Password reset email sent successfully");

    }

    public void sendVerificationEmail(User user, String token) throws MessagingException {
        String subject = "Account Verification - Finance Freedom";

        String htmlMessage = emailService.emailContent(token);

        emailService.sendEmail(user.getEmail(), subject, htmlMessage);
    }
    public void sendPasswordResetEmail(User user, String token) throws MessagingException{
        String subject = "Password Reset - Finance Freedom";

        String htmlMessage = emailService.emailContentResetPassword(token);

        emailService.sendEmail(user.getEmail(), subject, htmlMessage);
    }
}
