package finance.freedom.finance_freedom_backend.util;

import finance.freedom.finance_freedom_backend.exception.customexceptions.AccessDeniedException;
import finance.freedom.finance_freedom_backend.exception.customexceptions.UserNotFoundException;
import finance.freedom.finance_freedom_backend.model.security.CustomUserDetails;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuthorizationUtils {
    public static void validateOwnership(Integer resourceOwnerId, Integer authenticatedUserId) {
        log.debug("Checking ownership of resourceOwnerId: {} and authenticatedUserId: {}", resourceOwnerId, authenticatedUserId);
        if (!resourceOwnerId.equals(authenticatedUserId)) {
            log.warn("Unauthorized access attempt for resourceOwnerId: {} and authenticatedUserId: {}", resourceOwnerId, authenticatedUserId);
            throw new AccessDeniedException("Unauthorized");
        }
        log.debug("Validating ownership of resourceOwnerId: {} and authenticatedUserId: {}", resourceOwnerId, authenticatedUserId);
    }

    public static void requireUser(CustomUserDetails userDetails){
        log.debug("Checking if user is authenticated");
        if (userDetails == null || userDetails.getUser() == null) {
            log.warn("Authorization failed: userDetails or user is null");
            throw new UserNotFoundException("User not authenticated");
        }
        log.debug("Authenticated user: {}", userDetails.getUser().getUserId());
    }
}
