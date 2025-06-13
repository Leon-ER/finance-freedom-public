package finance.freedom.finance_freedom_backend;

import finance.freedom.finance_freedom_backend.dto.email.EmailVerificationTokenDTO;
import finance.freedom.finance_freedom_backend.dto.user.*;
import finance.freedom.finance_freedom_backend.enums.TokenPurpose;
import finance.freedom.finance_freedom_backend.exception.customexceptions.UserNotFoundException;
import finance.freedom.finance_freedom_backend.interfaces.email.IEmailService;
import finance.freedom.finance_freedom_backend.model.core.RefreshToken;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.model.security.CustomUserDetails;
import finance.freedom.finance_freedom_backend.model.security.JWT;
import finance.freedom.finance_freedom_backend.repository.RefreshTokenRepository;
import finance.freedom.finance_freedom_backend.repository.UserRepository;
import finance.freedom.finance_freedom_backend.service.core.AuthServiceImpl;
import finance.freedom.finance_freedom_backend.service.jwt.JWTServiceImpl;
import finance.freedom.finance_freedom_backend.service.security.CustomUserDetailsService;
import finance.freedom.finance_freedom_backend.util.AuthorizationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceImplTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private JWTServiceImpl jwtService;
    @Mock private UserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private IEmailService emailService;

    @InjectMocks private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void login_shouldReturnAuthenticatedUserResponseDTO() {
        LoginRequestDTO request = new LoginRequestDTO("test@example.com", "password", true);
        User user = new User();
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setVerified(true);

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication auth = mock(Authentication.class);


        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken(eq(user), any(Duration.class)))
                .thenReturn(new JWT("access-token", new Date(System.currentTimeMillis() + 900000)));

        AuthenticatedUserResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertNotNull(response.getAccessToken());
    }

    @Test
    void login_shouldThrowExceptionWhenUserNotVerified() {
        LoginRequestDTO request = new LoginRequestDTO("unverified@example.com", "password", true);
        User unverifiedUser = new User();
        unverifiedUser.setEmail("unverified@example.com");
        unverifiedUser.setFullName("Unverified User");
        unverifiedUser.setVerified(false);

        CustomUserDetails userDetails = new CustomUserDetails(unverifiedUser);
        Authentication auth = mock(Authentication.class);

        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            authService.login(request);
        });

        assertEquals("User is not verified", exception.getMessage());
    }
    @Test
    void passwordReset_shouldSendPasswordResetEmailAndReturnMessage() throws Exception {
        User user = new User();
        user.setEmail("reset@example.com");

        EmailVerificationTokenDTO mockToken = new EmailVerificationTokenDTO();
        mockToken.setToken("mock-reset-token");
        mockToken.setPurpose(TokenPurpose.PASSWORD_RESET);
        mockToken.setExpiresAt(LocalDateTime.now().plusMinutes(15));

        when(userRepository.findByEmail("reset@example.com")).thenReturn(user);
        when(emailService.generateEmailVerificationToken(user, TokenPurpose.PASSWORD_RESET)).thenReturn(mockToken);
        when(emailService.emailContentResetPassword("mock-reset-token"))
                .thenReturn("<html>reset-password-email-content</html>");
        doNothing().when(emailService).sendEmail(eq(user.getEmail()), anyString(), anyString());

        GenericResponse response = authService.passwordReset("reset@example.com");

        assertEquals("Password reset email sent successfully", response.getMessage());

        verify(emailService).generateEmailVerificationToken(user, TokenPurpose.PASSWORD_RESET);
        verify(emailService).emailContentResetPassword("mock-reset-token");
        verify(emailService).sendEmail(
                eq("reset@example.com"),
                eq("Password Reset - Finance Freedom"),
                eq("<html>reset-password-email-content</html>")
        );
    }

    @Test
    void passwordReset_shouldThrowWhenUserNotFound() {
        String invalidEmail = "nonexistent@example.com";

        when(userRepository.findByEmail(invalidEmail)).thenReturn(null);

        UserNotFoundException exception = assertThrows(UserNotFoundException.class, () -> {
            authService.passwordReset(invalidEmail);
        });

        assertEquals("User with email nonexistent@example.com not found", exception.getMessage());
        verify(userRepository).findByEmail(invalidEmail);
        verifyNoInteractions(emailService);
    }


}
