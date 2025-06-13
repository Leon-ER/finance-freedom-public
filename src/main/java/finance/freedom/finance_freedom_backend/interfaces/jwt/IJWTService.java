package finance.freedom.finance_freedom_backend.interfaces.jwt;

import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.security.JWT;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.time.Duration;

public interface IJWTService {
    JWT generateToken(User user, Duration duration);

    String extractUserName(String token);

    boolean validateToken(String token, UserDetails userDetails);

}
