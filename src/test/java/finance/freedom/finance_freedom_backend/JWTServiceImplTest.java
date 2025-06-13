package finance.freedom.finance_freedom_backend;

import finance.freedom.finance_freedom_backend.interfaces.aws.IAwsSecretsService;
import finance.freedom.finance_freedom_backend.model.aws.JWTSecretKey;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.security.JWT;
import finance.freedom.finance_freedom_backend.service.jwt.JWTServiceImpl;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JWTServiceImplTest {

    @Mock
    private IAwsSecretsService awsSecrets;

    @InjectMocks
    private JWTServiceImpl jwtService;

    private User user;

    private JWTSecretKey jwtSecretKey;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setEmail("test@example.com");
        user.setFullName("Test User");

        String mockSecret = "mock_test_jwt_secret_key_123456789012";
        jwtSecretKey = new JWTSecretKey();
        jwtSecretKey.setJwtSecret(mockSecret);

        when(awsSecrets.getSecretKey()).thenReturn(jwtSecretKey);
    }

    @Test
    void generateToken_shouldCreateValidJWT() {
        JWT jwt = jwtService.generateToken(user, Duration.ofMinutes(30));

        assertNotNull(jwt.getToken());
        assertNotNull(jwt.getExpiresAt());
        assertTrue(jwt.getExpiresAt().after(new Date()));
    }

    @Test
    void extractUserName_shouldReturnCorrectEmail() {
        JWT jwt = jwtService.generateToken(user, Duration.ofMinutes(30));

        String extracted = jwtService.extractUserName(jwt.getToken());

        assertEquals(user.getEmail(), extracted);
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        JWT jwt = jwtService.generateToken(user, Duration.ofMinutes(30));

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(user.getEmail());

        boolean result = jwtService.validateToken(jwt.getToken(), userDetails);

        assertTrue(result);
    }

    @Test
    void extractAllClaims_shouldContainEmailAndUsername() {
        JWT jwt = jwtService.generateToken(user, Duration.ofMinutes(30));
        Claims claims = jwtService.extractAllClaims(jwt.getToken());

        assertEquals(user.getEmail(), claims.get("email"));
        assertEquals(user.getFullName(), claims.get("username"));
    }

    @Test
    void validateToken_shouldReturnFalseForWrongUser() {
        JWT jwt = jwtService.generateToken(user, Duration.ofMinutes(30));

        UserDetails wrongUser = mock(UserDetails.class);
        when(wrongUser.getUsername()).thenReturn("wrong@example.com");

        boolean result = jwtService.validateToken(jwt.getToken(), wrongUser);

        assertFalse(result);
    }
}
