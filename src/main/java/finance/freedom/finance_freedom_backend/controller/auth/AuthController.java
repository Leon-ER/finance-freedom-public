package finance.freedom.finance_freedom_backend.controller.auth;

import finance.freedom.finance_freedom_backend.dto.user.*;
import finance.freedom.finance_freedom_backend.enums.RateLimitType;
import finance.freedom.finance_freedom_backend.enums.TokenPurpose;
import finance.freedom.finance_freedom_backend.interfaces.core.IAuthService;
import finance.freedom.finance_freedom_backend.interfaces.email.IEmailService;
import finance.freedom.finance_freedom_backend.interfaces.ratelimiter.IRateLimiterService;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.model.security.CustomUserDetails;
import jakarta.mail.IllegalWriteException;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Validated
@RequiredArgsConstructor
public class AuthController {
    private final IAuthService authService;

    private final IRateLimiterService rateLimiterService;

    private final IEmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<?> addUser(HttpServletRequest request,
                                     @Valid @RequestBody CreateUserDTO user) throws MessagingException {

        rateLimiterService.enforceRateLimit(request, RateLimitType.SIGNUP, "create user");


        return ResponseEntity.status(HttpStatus.CREATED).body(authService.save(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticatedUserResponseDTO> login(HttpServletRequest request,
                                        @RequestBody LoginRequestDTO loginRequestDTO) {

        rateLimiterService.enforceRateLimit(request, RateLimitType.LOGIN, "login user");

        return ResponseEntity.ok(authService.login(loginRequestDTO));

    }
    @PostMapping("password-reset")
    public ResponseEntity<GenericResponse> changePassword(HttpServletRequest request,
                                                          @RequestParam String email) throws MessagingException {

        rateLimiterService.enforceRateLimit(request,RateLimitType.PASSWORD_RESET,"password reset");

        if(email == null){
            throw new IllegalWriteException("Email can't be null for password reset");
        }

        return ResponseEntity.ok(authService.passwordReset(email));
    }

    @PostMapping("/verify")
    public ResponseEntity<GenericResponse> verifyEmail(@RequestParam String token,
                                                       @RequestParam TokenPurpose purpose) {

        return ResponseEntity.ok(emailService.verifyToken(token, purpose, null, null));
    }
}
