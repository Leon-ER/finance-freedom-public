package finance.freedom.finance_freedom_backend;

import finance.freedom.finance_freedom_backend.dto.savinggoal.CreateSavingGoalDTO;
import finance.freedom.finance_freedom_backend.dto.savinggoal.SavingGoalResponseDTO;
import finance.freedom.finance_freedom_backend.dto.savinggoal.UpdateSavingGoalDTO;
import finance.freedom.finance_freedom_backend.exception.customexceptions.AccessDeniedException;
import finance.freedom.finance_freedom_backend.exception.customexceptions.SavingGoalNotFoundException;
import finance.freedom.finance_freedom_backend.model.core.SavingGoal;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.repository.SavingGoalRepository;
import finance.freedom.finance_freedom_backend.service.core.SavingGoalServiceImpl;
import finance.freedom.finance_freedom_backend.util.AuthorizationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SavingGoalServiceImplTest {

    @Mock private SavingGoalRepository savingGoalRepository;
    @InjectMocks private SavingGoalServiceImpl savingGoalService;

    private User user;
    private SavingGoal goal;
    private Integer goalId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User();
        user.setUserId(1);
        user.setEmail("test@example.com");

        goalId = 101;

        goal = new SavingGoal();
        goal.setGoalId(goalId);
        goal.setUser(user);
        goal.setGoalName("Vacation");
        goal.setTargetAmount(BigDecimal.valueOf(1000));
        goal.setCurrentAmount(BigDecimal.valueOf(250));
    }

    @Test
    void getSavingGoalById_shouldThrowIfUnauthorized() {
        when(savingGoalRepository.findByGoalId(goalId)).thenReturn(goal);

        try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
            utils.when(() -> AuthorizationUtils.validateOwnership(
                    any(), any()
            )).thenThrow(new AccessDeniedException("Unauthorized"));

            AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                    savingGoalService.getSavingGoalById(user, goalId));

            assertEquals("Unauthorized", ex.getMessage());
        }
    }

    @Test
    void getSavingGoalById_shouldReturnDTO() {
        when(savingGoalRepository.findByGoalId(goalId)).thenReturn(goal);

        try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
            utils.when(() -> AuthorizationUtils.validateOwnership(
                    eq(goal.getUser().getUserId()), eq(user.getUserId())
            )).thenCallRealMethod();

            SavingGoalResponseDTO dto = savingGoalService.getSavingGoalById(user, goalId);
            assertEquals("Vacation", dto.getGoalName());
        }
    }

    @Test
    void getSavingGoalById_shouldThrowIfNotFound() {
        when(savingGoalRepository.findByGoalId(goalId)).thenReturn(null);
        assertThrows(SavingGoalNotFoundException.class, () ->
                savingGoalService.getSavingGoalById(user, goalId));
    }

    @Test
    void getSavingGoal_shouldReturnMapOfDTOs() {
        when(savingGoalRepository.findByUser(user)).thenReturn(List.of(goal));
        Map<Integer, SavingGoalResponseDTO> result = savingGoalService.getSavingGoal(user);
        assertTrue(result.containsKey(goalId));
        assertEquals("Vacation", result.get(goalId).getGoalName());
    }

    @Test
    void updateSavingGoal_shouldApplyChanges() {
        UpdateSavingGoalDTO dto = new UpdateSavingGoalDTO();
        dto.setGoalName("Emergency Fund");
        dto.setCurrentAmount(BigDecimal.valueOf(500));

        when(savingGoalRepository.findByGoalId(goalId)).thenReturn(goal);

        try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
            utils.when(() -> AuthorizationUtils.validateOwnership(
                    eq(goal.getUser().getUserId()), eq(user.getUserId())
            )).thenCallRealMethod();

            SavingGoalResponseDTO updated = savingGoalService.updateSavingGoal(user, goalId, dto);
            assertEquals("Emergency Fund", updated.getGoalName());
            assertEquals(BigDecimal.valueOf(500), updated.getCurrentAmount());
        }
    }

    @Test
    void deleteSavingGoal_shouldReturnSuccessMessage() {
        when(savingGoalRepository.findByGoalId(goalId)).thenReturn(goal);

        try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
            utils.when(() -> AuthorizationUtils.validateOwnership(
                    eq(goal.getUser().getUserId()), eq(user.getUserId())
            )).thenCallRealMethod();

            GenericResponse response = savingGoalService.deleteSavingGoal(user, goalId);

            assertNotNull(response);
            assertTrue(response.getMessage().contains("deleted successfully"));
            assertNotNull(response.getTimestamp());
            verify(savingGoalRepository).delete(goal);
        }
    }

    @Test
    void deleteSavingGoal_shouldThrowIfNotFound() {
        when(savingGoalRepository.findByGoalId(goalId)).thenReturn(null);
        assertThrows(SavingGoalNotFoundException.class, () ->
                savingGoalService.deleteSavingGoal(user, goalId));
    }
}
