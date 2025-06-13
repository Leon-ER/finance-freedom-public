package finance.freedom.finance_freedom_backend.controller.transaction;

import finance.freedom.finance_freedom_backend.dto.transaction.CreateTransactionDTO;
import finance.freedom.finance_freedom_backend.dto.transaction.TransactionResponseDTO;
import finance.freedom.finance_freedom_backend.dto.transaction.UpdateTransactionDTO;
import finance.freedom.finance_freedom_backend.enums.RateLimitType;
import finance.freedom.finance_freedom_backend.enums.TransactionType;
import finance.freedom.finance_freedom_backend.interfaces.core.ITransactionService;
import finance.freedom.finance_freedom_backend.interfaces.ratelimiter.IRateLimiterService;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.model.security.CustomUserDetails;
import finance.freedom.finance_freedom_backend.util.AuthorizationUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Validated
@Slf4j
@RequestMapping("/api/transaction")
public class TransactionController {

    private final ITransactionService transactionService;

    private final IRateLimiterService rateLimiterService;


    @PostMapping()
    public ResponseEntity<TransactionResponseDTO> create(HttpServletRequest request,
                                                  @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                  @Valid @RequestBody CreateTransactionDTO transaction) {
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(request, RateLimitType.TRANSACTION, "save transaction");

        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.save(customUserDetails.getUser(), transaction));
    }

    @GetMapping("/{transactionID}")
    public ResponseEntity<TransactionResponseDTO> getById(HttpServletRequest request,
                                                                 @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                 @PathVariable Integer transactionID) {
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(request, RateLimitType.TRANSACTION, "get transaction");

        return ResponseEntity.ok(transactionService.getTransaction(customUserDetails.getUser(),transactionID));
    }

    @GetMapping()
    public ResponseEntity<Map<Integer, TransactionResponseDTO>> getFilteredOrAll(HttpServletRequest request,
                                                                             @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                             @RequestParam(required = false) TransactionType transactionType,
                                                                             @RequestParam(required = false) LocalDateTime startDate,
                                                                             @RequestParam(required = false) LocalDateTime endDate) {
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(request, RateLimitType.TRANSACTION, "get transactions expense");

        return ResponseEntity.ok(transactionService.getFilteredTransactions(
                customUserDetails.getUser(), transactionType, startDate, endDate));
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<GenericResponse> delete(HttpServletRequest request,
                                                             @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                             @PathVariable Integer transactionId) {
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(request, RateLimitType.TRANSACTION, "delete transaction");


        return ResponseEntity.ok(transactionService.deleteTransaction(customUserDetails.getUser(),transactionId));
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDTO> update(HttpServletRequest request,
                                                                    @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                    @PathVariable Integer transactionId,
                                                                    @Valid @RequestBody UpdateTransactionDTO updatedTransaction) {
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(request, RateLimitType.TRANSACTION, "update transaction");

        return ResponseEntity.ok(transactionService.updateTransaction(customUserDetails.getUser(), transactionId,updatedTransaction));
    }
}
