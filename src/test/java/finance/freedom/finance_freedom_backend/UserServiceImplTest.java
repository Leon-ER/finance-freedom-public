package finance.freedom.finance_freedom_backend;

import finance.freedom.finance_freedom_backend.dto.email.EmailVerificationTokenDTO;
import finance.freedom.finance_freedom_backend.dto.user.RefreshTokenRequest;
import finance.freedom.finance_freedom_backend.dto.user.UserDetailsRequestDTO;
import finance.freedom.finance_freedom_backend.enums.TokenPurpose;
import finance.freedom.finance_freedom_backend.exception.customexceptions.UserNotFoundException;
import finance.freedom.finance_freedom_backend.interfaces.aws.IAwsS3Service;
import finance.freedom.finance_freedom_backend.interfaces.email.IEmailService;
import finance.freedom.finance_freedom_backend.model.core.RefreshToken;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.model.security.CustomUserDetails;
import finance.freedom.finance_freedom_backend.model.security.JWT;
import finance.freedom.finance_freedom_backend.repository.RefreshTokenRepository;
import finance.freedom.finance_freedom_backend.repository.UserRepository;
import finance.freedom.finance_freedom_backend.service.core.UserServiceImpl;
import finance.freedom.finance_freedom_backend.service.jwt.JWTServiceImpl;
import finance.freedom.finance_freedom_backend.service.security.CustomUserDetailsService;
import finance.freedom.finance_freedom_backend.util.AuthorizationUtils;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
class UserServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private JWTServiceImpl jwtService;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private CustomUserDetailsService customUserDetailsService;
    @Mock private IAwsS3Service s3Service;
    @Mock private IEmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setUserId(1);
        user.setEmail("leon@example.com");
        user.setFullName("Leon Dev");
    }

    @Test
    void getUserDetails_shouldReturnUserDetailsRequestDTO_whenUserIsValid() {
        UserDetailsRequestDTO dto = userService.getUserDetails(user);

        assertNotNull(dto);
        assertEquals("Leon Dev", dto.getFullName());
        assertEquals("leon@example.com", dto.getEmail());
    }

    @Test
    void deleteAccount_shouldSendDeletionEmailAndReturnMessage_whenUserIsValid() throws MessagingException {
        EmailVerificationTokenDTO mockToken = new EmailVerificationTokenDTO();
        mockToken.setToken("mock-token");
        mockToken.setPurpose(TokenPurpose.ACCOUNT_DELETION);
        mockToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        when(emailService.generateEmailVerificationToken(user, TokenPurpose.ACCOUNT_DELETION)).thenReturn(mockToken);
        when(emailService.emailContentDeleteAccount("mock-token")).thenReturn("<html>mock-token</html>");
        doNothing().when(emailService).sendEmail(eq(user.getEmail()), anyString(), anyString());

        GenericResponse result = userService.deleteAccount(user);

        assertEquals("Account deletion email sent successfully", result.getMessage());
        verify(emailService).generateEmailVerificationToken(user, TokenPurpose.ACCOUNT_DELETION);
        verify(emailService).emailContentDeleteAccount("mock-token");
        verify(emailService).sendEmail(eq("leon@example.com"), eq("Account deletion - Finance Freedom"), eq("<html>mock-token</html>"));
    }

    @Test
    void refresh_shouldReturnNewAccessToken() {
        String tokenStr = "valid-refresh-token";
        RefreshTokenRequest request = new RefreshTokenRequest(tokenStr);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setRefreshToken(tokenStr);
        refreshToken.setUser(user);

        CustomUserDetails userDetails = new CustomUserDetails(user);

        when(jwtService.extractUserName(tokenStr)).thenReturn(user.getEmail());
        when(refreshTokenRepository.findByRefreshToken(tokenStr)).thenReturn(refreshToken);
        when(customUserDetailsService.loadUserByUsername(user.getEmail())).thenReturn(userDetails);

        when(jwtService.validateToken(tokenStr, userDetails)).thenReturn(true);

        when(jwtService.generateToken(eq(user), any(Duration.class)))
                .thenReturn(new JWT("new-access-token", new Date(System.currentTimeMillis() + 900_000)));

        try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
            utils.when(() -> AuthorizationUtils.validateOwnership(
                    eq(user.getUserId()), eq(user.getUserId())
            )).thenAnswer(invocation -> null);

            JWT result = userService.refresh(user, request);

            assertNotNull(result);
            assertEquals("new-access-token", result.getToken());
        }
    }



    @Test
    void logout_shouldDeleteRefreshToken() {
        RefreshToken token = new RefreshToken();
        token.setRefreshToken("token123");
        token.setUser(user);

        when(refreshTokenRepository.findByRefreshToken("token123")).thenReturn(token);

        RefreshTokenRequest request = new RefreshTokenRequest("token123");

        try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
            utils.when(() -> AuthorizationUtils.validateOwnership(
                    eq(token.getUser().getUserId()), eq(user.getUserId())
            )).thenCallRealMethod();

            GenericResponse result = userService.logout(user, request);

            assertEquals("Logout successful", result.getMessage());
            verify(refreshTokenRepository, times(1)).delete(token);
        }
    }

    @Test
    void resetPassword_shouldSendPasswordChangeEmailAndReturnMessage() throws MessagingException {
        EmailVerificationTokenDTO mockToken = new EmailVerificationTokenDTO();
        mockToken.setToken("mock-reset-token");
        mockToken.setPurpose(TokenPurpose.PASSWORD_CHANGE);
        mockToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        when(emailService.generateEmailVerificationToken(user, TokenPurpose.PASSWORD_CHANGE)).thenReturn(mockToken);
        when(emailService.emailContentChangePassword("mock-reset-token")).thenReturn("<html>reset-password-content</html>");
        doNothing().when(emailService).sendEmail(eq(user.getEmail()), anyString(), anyString());

        GenericResponse result = userService.resetPassword(user);

        assertEquals("Password change email sent successfully", result.getMessage());
        verify(emailService).generateEmailVerificationToken(user, TokenPurpose.PASSWORD_CHANGE);
        verify(emailService).emailContentChangePassword("mock-reset-token");
        verify(emailService).sendEmail(eq("leon@example.com"), eq("Password reset - Finance Freedom"), eq("<html>reset-password-content</html>"));
    }
}
