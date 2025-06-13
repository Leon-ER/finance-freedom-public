package finance.freedom.finance_freedom_backend.controller.savinggoal;

import finance.freedom.finance_freedom_backend.dto.savinggoal.CreateSavingGoalDTO;
import finance.freedom.finance_freedom_backend.dto.savinggoal.SavingGoalResponseDTO;
import finance.freedom.finance_freedom_backend.dto.savinggoal.UpdateSavingGoalDTO;
import finance.freedom.finance_freedom_backend.enums.RateLimitType;
import finance.freedom.finance_freedom_backend.interfaces.core.ISavingGoalService;
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
@RequestMapping("/api/saving-goal")
@Validated
@RequiredArgsConstructor
public class SavingGoalController {

    private final ISavingGoalService savingGoalService;

    private final IRateLimiterService rateLimiterService;

    @PostMapping()
    public ResponseEntity<SavingGoalResponseDTO> create(HttpServletRequest request,
                                                                @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                @Valid @RequestBody CreateSavingGoalDTO savingGoal){
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(request, RateLimitType.SAVING_GOAL, "save saving goal");

        return ResponseEntity.status(HttpStatus.CREATED).body(savingGoalService.save(customUserDetails.getUser(), savingGoal));
    }

    @GetMapping("/{savingGoalId}")
    public ResponseEntity<SavingGoalResponseDTO> getById(HttpServletRequest request,
                                                                   @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                   @PathVariable Integer savingGoalId){
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(request, RateLimitType.SAVING_GOAL, "get saving goal by id");

        return ResponseEntity.ok(savingGoalService.getSavingGoalById(customUserDetails.getUser(),savingGoalId));
    }

    @GetMapping()
    public ResponseEntity<Map<Integer, SavingGoalResponseDTO>> getAll(HttpServletRequest request,
                                                                           @AuthenticationPrincipal CustomUserDetails customUserDetails){
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(request, RateLimitType.SAVING_GOAL, "get saving goal");

        return ResponseEntity.ok(savingGoalService.getSavingGoal(customUserDetails.getUser()));
    }
    @PutMapping("/{savingGoalId}")
    public ResponseEntity<SavingGoalResponseDTO> update(HttpServletRequest request,
                                                                  @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                                  @PathVariable Integer savingGoalId,
                                                                  @RequestBody UpdateSavingGoalDTO savingGoal){
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(request, RateLimitType.SAVING_GOAL, "update saving goal");

        return ResponseEntity.ok(savingGoalService.updateSavingGoal(customUserDetails.getUser(),savingGoalId, savingGoal));
    }
    @DeleteMapping("/{savingGoalId}")
    public ResponseEntity<GenericResponse> delete(HttpServletRequest request,
                                                            @AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                            @PathVariable Integer savingGoalId){
        AuthorizationUtils.requireUser(customUserDetails);

        rateLimiterService.enforceRateLimit(request, RateLimitType.SAVING_GOAL, "delete saving goal");

        return ResponseEntity.ok(savingGoalService.deleteSavingGoal(customUserDetails.getUser(),savingGoalId));
    }
}
