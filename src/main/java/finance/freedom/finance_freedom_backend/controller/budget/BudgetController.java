package finance.freedom.finance_freedom_backend.controller.budget;

import finance.freedom.finance_freedom_backend.dto.budget.BudgetResponseDTO;
import finance.freedom.finance_freedom_backend.dto.budget.CreateBudgetDTO;
import finance.freedom.finance_freedom_backend.dto.budget.UpdateBudgetDTO;
import finance.freedom.finance_freedom_backend.enums.RateLimitType;
import finance.freedom.finance_freedom_backend.interfaces.core.IBudgetService;
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
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@Valid
@RequiredArgsConstructor
@RequestMapping("/api/budget")
public class BudgetController {

    private final IBudgetService budgetService;

    private final IRateLimiterService rateLimiterService;

    @PostMapping()
    public ResponseEntity<BudgetResponseDTO> create(HttpServletRequest request,
                                                  @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                  @Valid @RequestBody CreateBudgetDTO budgetRequestDTO){
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(request, RateLimitType.BUDGET, "save budget");

        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.save(customUserDetails.getUser(), budgetRequestDTO));
    }

    @GetMapping("/{budgetId}")
    public ResponseEntity<BudgetResponseDTO> getById(HttpServletRequest request,
                                                     @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                     @PathVariable Integer budgetId){
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(request, RateLimitType.BUDGET, "get budget by id");

        return ResponseEntity.ok(budgetService.getById(customUserDetails.getUser(), budgetId));
    }

    @PutMapping("/{budgetId}")
    public ResponseEntity<BudgetResponseDTO> update(HttpServletRequest request,
                                                    @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                    @PathVariable Integer budgetId,
                                                    @Valid @RequestBody UpdateBudgetDTO budgetRequestDTO){
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(request, RateLimitType.BUDGET, "update budget by id");

        return ResponseEntity.ok(budgetService.update(customUserDetails.getUser(), budgetId, budgetRequestDTO));
    }

    @DeleteMapping("/{budgetId}")
    public ResponseEntity<GenericResponse> delete(HttpServletRequest request,
                                                  @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                  @PathVariable Integer budgetId){
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(request, RateLimitType.BUDGET, "delete budget by id");

        return ResponseEntity.ok(budgetService.deleteById(customUserDetails.getUser(), budgetId));
    }

    @GetMapping()
    public ResponseEntity<Map<Integer,BudgetResponseDTO>> getAll(HttpServletRequest request,
                                                              @AuthenticationPrincipal CustomUserDetails customUserDetails){
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(request, RateLimitType.BUDGET, "get budget");

        return ResponseEntity.ok(budgetService.getBudgetsByUser(customUserDetails.getUser()));
    }

}
