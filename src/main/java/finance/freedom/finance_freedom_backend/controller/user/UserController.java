package finance.freedom.finance_freedom_backend.controller.user;

import finance.freedom.finance_freedom_backend.dto.user.RefreshTokenRequest;
import finance.freedom.finance_freedom_backend.enums.RateLimitType;
import finance.freedom.finance_freedom_backend.enums.TokenPurpose;
import finance.freedom.finance_freedom_backend.interfaces.core.IUserService;
import finance.freedom.finance_freedom_backend.interfaces.email.IEmailService;
import finance.freedom.finance_freedom_backend.interfaces.ratelimiter.IRateLimiterService;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.model.security.CustomUserDetails;
import finance.freedom.finance_freedom_backend.util.AuthorizationUtils;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Validated
@RequiredArgsConstructor
public class UserController {

    private final IUserService userService;

    private final IRateLimiterService rateLimiterService;

    private final IEmailService emailService;

    @GetMapping()
    public ResponseEntity<?> getUserDetails(HttpServletRequest request,  @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(request, RateLimitType.DEFAULT, "get user details");

        return ResponseEntity.ok(userService.getUserDetails(customUserDetails.getUser()));
    }

    @DeleteMapping()
    public ResponseEntity<GenericResponse> deleteAccount(HttpServletRequest request, @AuthenticationPrincipal CustomUserDetails customUserDetails) throws MessagingException {
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(request, RateLimitType.DELETE_USER, "delete user account");

        return ResponseEntity.ok(userService.deleteAccount(customUserDetails.getUser()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(HttpServletRequest request,@AuthenticationPrincipal CustomUserDetails customUserDetails, @Valid @RequestBody RefreshTokenRequest refreshToken) {

        rateLimiterService.enforceRateLimit(request, RateLimitType.REFRESH_TOKEN, "refresh token");

        String accessToken = userService.refresh(customUserDetails.getUser(),refreshToken).getToken();
        return ResponseEntity.ok(String.format("User logged in successfully, %s", accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<GenericResponse> logout(@AuthenticationPrincipal CustomUserDetails customUserDetails, @Valid @RequestBody RefreshTokenRequest refreshToken) {

        return ResponseEntity.ok(userService.logout(customUserDetails.getUser(),refreshToken));
    }

    @PostMapping("password-change")
    public ResponseEntity<GenericResponse> changePassword(HttpServletRequest request,
                                                 @AuthenticationPrincipal CustomUserDetails customUserDetails) throws MessagingException {

        rateLimiterService.enforceRateLimit(request,RateLimitType.PASSWORD_CHANGE,"password change");

        return ResponseEntity.ok(userService.resetPassword(customUserDetails.getUser()));
    }

    @PostMapping("/verify")
    public ResponseEntity<GenericResponse> verifyToken(@RequestParam String token,
                                                       @RequestParam TokenPurpose purpose,
                                                       @RequestParam(required = false)String newPassword,
                                                       @RequestParam(required = false) String oldPassword) {

        if(purpose == TokenPurpose.PASSWORD_RESET){
            if(newPassword == null){
                throw new IllegalArgumentException("New password must not be null for password reset");
            }
        }

        if(purpose == TokenPurpose.PASSWORD_CHANGE){
            if(newPassword == null || oldPassword == null){
                throw new IllegalArgumentException("New or old password must not be null for password change.");
            }
        }

        return ResponseEntity.ok(emailService.verifyToken(token, purpose, newPassword, oldPassword));
    }
}
