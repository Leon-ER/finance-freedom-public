package finance.freedom.finance_freedom_backend.interfaces.ratelimiter;

import finance.freedom.finance_freedom_backend.enums.RateLimitType;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;


public interface IRateLimiterService {
    Bucket resolveBucket (HttpServletRequest request, RateLimitType type);

    Bandwidth getLimitForType(RateLimitType type);

    void enforceRateLimit(HttpServletRequest request, RateLimitType type, String action);
}
