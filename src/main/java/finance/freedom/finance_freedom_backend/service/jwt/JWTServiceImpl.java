package finance.freedom.finance_freedom_backend.service.jwt;

import finance.freedom.finance_freedom_backend.interfaces.aws.IAwsSecretsService;
import finance.freedom.finance_freedom_backend.interfaces.jwt.IJWTService;
import finance.freedom.finance_freedom_backend.model.aws.JWTSecretKey;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.security.JWT;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class JWTServiceImpl implements IJWTService {

    private final IAwsSecretsService awsSecrets;


    public JWT generateToken(User user, Duration duration) {
        log.info("Generating token for user {}", user.getEmail());
        JWT jwt = new JWT();
        Map<String, Object> claims = new HashMap<>();

        claims.put("email", user.getEmail());
        claims.put("username", user.getFullName());

        LocalDateTime issuedAt = LocalDateTime.now();
        LocalDateTime expiration = issuedAt.plus(duration);

        Date issuedAtDate = java.util.Date.from(issuedAt.atZone(java.time.ZoneId.systemDefault()).toInstant());
        Date expirationDate = java.util.Date.from(expiration.atZone(java.time.ZoneId.systemDefault()).toInstant());

        String token = Jwts.builder()
                .claims(claims)
                .subject(user.getEmail())
                .issuedAt(issuedAtDate)
                .expiration(expirationDate)
                .signWith(getKey())
                .compact();

        jwt.setToken(token);
        jwt.setExpiresAt(expirationDate);

        log.info("Token generated successfully");

        return jwt;
    }

    private SecretKey getKey(){
        JWTSecretKey jwtSecretKey = awsSecrets.getSecretKey();
        byte[] encodedKey = Base64.getEncoder().encode(jwtSecretKey.getJwtSecret().getBytes());
        return Keys.hmacShaKeyFor(encodedKey);
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUserName(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
