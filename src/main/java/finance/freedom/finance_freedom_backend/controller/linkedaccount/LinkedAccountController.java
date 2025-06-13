package finance.freedom.finance_freedom_backend.controller.linkedaccount;

import finance.freedom.finance_freedom_backend.dto.linkedaccount.CreateLinkedAccountDTO;
import finance.freedom.finance_freedom_backend.dto.linkedaccount.LinkedAccountResponseDTO;
import finance.freedom.finance_freedom_backend.dto.linkedaccount.UpdateLinkedAccountDTO;
import finance.freedom.finance_freedom_backend.enums.RateLimitType;
import finance.freedom.finance_freedom_backend.interfaces.core.ILinkedAccountService;
import finance.freedom.finance_freedom_backend.interfaces.ratelimiter.IRateLimiterService;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.model.security.CustomUserDetails;
import finance.freedom.finance_freedom_backend.util.AuthorizationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/linked-account")
@RequiredArgsConstructor
@Validated
public class LinkedAccountController {
    private final ILinkedAccountService linkedAccountService;

    private final IRateLimiterService rateLimiterService;

    @PostMapping()
    public ResponseEntity<LinkedAccountResponseDTO> create(HttpServletRequest servlet,
                                                           @AuthenticationPrincipal CustomUserDetails customUserDetails
            ,@Valid @RequestBody CreateLinkedAccountDTO createLinkedAccountDTO){
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(servlet, RateLimitType.LINKED_ACCOUNT, "save linked account");


        return ResponseEntity.status(HttpStatus.CREATED).body(linkedAccountService.createLinkedAccount(customUserDetails.getUser(), createLinkedAccountDTO));
    }

    @GetMapping()
    public ResponseEntity<Map<Integer,LinkedAccountResponseDTO>> getAll(HttpServletRequest servlet,
                                                                     @AuthenticationPrincipal CustomUserDetails customUserDetails){
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(servlet, RateLimitType.LINKED_ACCOUNT, "get linked account by user");

        return ResponseEntity.ok(linkedAccountService.getLinkedAccountByUser(customUserDetails.getUser()));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<LinkedAccountResponseDTO> getById(HttpServletRequest servlet,
                                                            @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                            @PathVariable Integer accountId){
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(servlet, RateLimitType.LINKED_ACCOUNT, "get linked account by id");

        return ResponseEntity.ok(linkedAccountService.getLinkedAccountById(customUserDetails.getUser(), accountId));
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<LinkedAccountResponseDTO> update(HttpServletRequest servlet,
                                                           @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                           @PathVariable Integer accountId,
                                                           @Valid @RequestBody UpdateLinkedAccountDTO updateLinkedAccountDTO){
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(servlet, RateLimitType.LINKED_ACCOUNT, "update linked account by id");

        return ResponseEntity.ok(linkedAccountService.updateLinkedAccount(customUserDetails.getUser(), accountId, updateLinkedAccountDTO));
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<GenericResponse> delete(HttpServletRequest servlet,
                                                  @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                  @PathVariable Integer accountId){
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(servlet, RateLimitType.LINKED_ACCOUNT, "delete linked account by id");

        return ResponseEntity.ok(linkedAccountService.deleteLinkedAccount(customUserDetails.getUser(), accountId));
    }
}
