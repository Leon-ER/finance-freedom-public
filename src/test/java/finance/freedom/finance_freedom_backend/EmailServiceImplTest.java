package finance.freedom.finance_freedom_backend;

import finance.freedom.finance_freedom_backend.dto.email.EmailVerificationTokenDTO;
import finance.freedom.finance_freedom_backend.enums.TokenPurpose;
import finance.freedom.finance_freedom_backend.interfaces.aws.IAwsS3Service;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.email.EmailVerificationToken;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.repository.EmailVerificationTokenRepository;
import finance.freedom.finance_freedom_backend.repository.RefreshTokenRepository;
import finance.freedom.finance_freedom_backend.repository.UserRepository;
import finance.freedom.finance_freedom_backend.service.email.EmailServiceImpl;
import finance.freedom.finance_freedom_backend.util.PasswordUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.mail.javamail.JavaMailSender;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceImplTest {

    @Mock private JavaMailSender javaMailSender;
    @Mock private UserRepository userRepository;
    @Mock private EmailVerificationTokenRepository tokenRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private IAwsS3Service s3Service;
    @Mock private PasswordUtil passwordUtil;

    @InjectMocks private EmailServiceImpl emailService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setVerified(false);
    }

    @Test
    void sendEmail_shouldSendMessage() throws MessagingException {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail(user.getEmail(), "Subject", "Body");

        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    @Test
    void generateEmailVerificationToken_shouldSaveTokenAndReturnDTO() {
        ArgumentCaptor<EmailVerificationToken> captor = ArgumentCaptor.forClass(EmailVerificationToken.class);

        EmailVerificationTokenDTO dto = emailService.generateEmailVerificationToken(user, TokenPurpose.EMAIL_VERIFICATION);

        verify(tokenRepository, times(1)).save(captor.capture());
        assertNotNull(dto.getToken());
        assertEquals(dto.getToken(), captor.getValue().getToken());
        assertEquals(user, captor.getValue().getUser());
    }

    @Test
    void verifyToken_shouldThrowForInvalidToken() {
        when(tokenRepository.findByToken("bad-token")).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            emailService.verifyToken("bad-token", TokenPurpose.EMAIL_VERIFICATION, null, null);
        });

        assertEquals("Invalid verification token", exception.getMessage());
    }


    @Test
    void verifyToken_shouldThrowForExpiredToken() {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        when(tokenRepository.findByToken("expired-token")).thenReturn(token);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            emailService.verifyToken("expired-token", TokenPurpose.EMAIL_VERIFICATION, null, null);
        });

        assertEquals("Verification token expired", exception.getMessage());
    }


    @Test
    void verifyToken_shouldVerifyUser() {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setPurpose(TokenPurpose.EMAIL_VERIFICATION);
        token.setUser(user);
        when(tokenRepository.findByToken("valid-token")).thenReturn(token);

        GenericResponse result = emailService.verifyToken("valid-token", TokenPurpose.EMAIL_VERIFICATION, null, null);

        assertEquals("Email verified successfully", result.getMessage());
        verify(userRepository).save(user);
        verify(tokenRepository).delete(token);
    }

    @Test
    void verifyToken_shouldChangePassword() {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setPurpose(TokenPurpose.PASSWORD_CHANGE);
        token.setUser(user);
        user.setPasswordHash("old-hash");

        when(tokenRepository.findByToken("change-token")).thenReturn(token);
        when(passwordUtil.verify("oldpass", "old-hash")).thenReturn(true);
        when(passwordUtil.hash("newpass")).thenReturn("new-hash");

        GenericResponse result = emailService.verifyToken("change-token", TokenPurpose.PASSWORD_CHANGE, "newpass", "oldpass");

        assertEquals("Password changed successfully", result.getMessage());
        assertEquals("new-hash", user.getPasswordHash());

        verify(userRepository).save(user);
        verify(tokenRepository).delete(token);
        verify(passwordUtil).verify("oldpass", "old-hash");
        verify(passwordUtil).hash("newpass");
    }


    @Test
    void verifyToken_shouldResetPassword() {
        EmailVerificationToken token = new EmailVerificationToken();
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        token.setPurpose(TokenPurpose.PASSWORD_RESET);
        token.setUser(user);

        when(tokenRepository.findByToken("reset-token")).thenReturn(token);
        when(passwordUtil.hash("new-reset-pass")).thenReturn("hashed-reset");

        GenericResponse result = emailService.verifyToken("reset-token", TokenPurpose.PASSWORD_RESET, "new-reset-pass", null);

        assertEquals("Password changed successfully", result.getMessage());
        assertEquals("hashed-reset", user.getPasswordHash());

        verify(userRepository).save(user);
        verify(tokenRepository).delete(token);
        verify(passwordUtil).hash("new-reset-pass");
    }




    @Test
    void resendVerificationToken_shouldReturnUserNotFound() throws MessagingException {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(null);

        String result = emailService.resendVerificationToken("missing@example.com");

        assertEquals("User not found", result);
    }

    @Test
    void resendVerificationToken_shouldReturnAlreadyVerified() throws MessagingException {
        user.setVerified(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);

        String result = emailService.resendVerificationToken(user.getEmail());

        assertEquals("User already verified", result);
    }

    @Test
    void resendVerificationToken_shouldResendToken() throws MessagingException {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(user);
        when(javaMailSender.createMimeMessage()).thenReturn(mock(MimeMessage.class));

        String result = emailService.resendVerificationToken(user.getEmail());

        assertEquals("Verification email has been resent successfully.", result);
        verify(tokenRepository).deleteByUser(user);
        verify(tokenRepository).save(any(EmailVerificationToken.class));
        verify(javaMailSender).send(any(MimeMessage.class));
    }

    @Test
    void emailContent_shouldContainToken() {
        String token = "abc123";
        String html = emailService.emailContent(token);

        assertTrue(html.contains("abc123"));
        assertTrue(html.contains("Verify Email"));
    }
}
