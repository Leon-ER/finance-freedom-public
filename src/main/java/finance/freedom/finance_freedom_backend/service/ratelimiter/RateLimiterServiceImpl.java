package finance.freedom.finance_freedom_backend.service.ratelimiter;

import finance.freedom.finance_freedom_backend.enums.RateLimitType;
import finance.freedom.finance_freedom_backend.exception.customexceptions.TooManyRequestsException;
import finance.freedom.finance_freedom_backend.interfaces.ratelimiter.IRateLimiterService;
import finance.freedom.finance_freedom_backend.model.security.CustomUserDetails;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
public class RateLimiterServiceImpl implements IRateLimiterService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public Bucket resolveBucket(HttpServletRequest request, RateLimitType type) {

        String key = resolveKeyFromContext(request, type);
        return buckets.computeIfAbsent(key, k -> {
            Bandwidth limit = getLimitForType(type);
            return Bucket.builder().addLimit(limit).build();
        });
    }

    @Override
    public Bandwidth getLimitForType(RateLimitType type) {

        return switch (type) {
            case LOGIN, SIGNUP -> createLimit(5, 1, Duration.ofMinutes(1));
            case TRANSACTION -> createLimit(100, 100, Duration.ofMinutes(1));
            case BUDGET, SAVING_GOAL -> createLimit(20, 5, Duration.ofMinutes(5));
            case REFRESH_TOKEN -> createLimit(2, 1, Duration.ofMinutes(10));
            case LINKED_ACCOUNT -> createLimit(20, 20, Duration.ofMinutes(10));
            case DELETE_USER, PASSWORD_CHANGE -> createLimit(1, 1, Duration.ofHours(1));
            case VERIFY_TOKEN -> createLimit(5,1,Duration.ofMinutes(10));
            case PASSWORD_RESET -> createLimit(3,1,Duration.ofHours(24));
            default -> createLimit(20, 20, Duration.ofMinutes(1));
        };
    }

    public Bandwidth createLimit(int capacity, int refillTokens, Duration refillDuration) {

        return Bandwidth.builder()
                .capacity(capacity)
                .refillIntervally(refillTokens, refillDuration)
                .build();
    }

    public String resolveKeyFromContext(HttpServletRequest request, RateLimitType type) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
            return userDetails.getUser().getUserId() + "-" + type.name();
        }
        return getClientIP(request) + "-" + type.name();
    }

    private String getClientIP(HttpServletRequest request) {

        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    @Override
    public void enforceRateLimit(HttpServletRequest request, RateLimitType type, String action) {
        Bucket bucket = resolveBucket(request, type);
        if(!bucket.tryConsume(1)){
            log.warn("Too many {} attempts for user {}", action.toLowerCase(), getClientIP(request));
            throw new TooManyRequestsException(String.format("Too many %s attempts. Please try again later.", action.toLowerCase()));
        }
    }
}
