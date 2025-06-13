package finance.freedom.finance_freedom_backend;

import finance.freedom.finance_freedom_backend.dto.budget.*;
import finance.freedom.finance_freedom_backend.enums.CategoryType;
import finance.freedom.finance_freedom_backend.exception.customexceptions.*;
import finance.freedom.finance_freedom_backend.model.core.Budget;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.repository.BudgetRepository;
import finance.freedom.finance_freedom_backend.service.core.BudgetServiceImpl;
import finance.freedom.finance_freedom_backend.util.AuthorizationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BudgetServiceImplTest {

    @Mock private BudgetRepository budgetRepository;
    @InjectMocks private BudgetServiceImpl budgetService;
    private User user;
    private Integer budgetId;
    private Budget budget;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setUserId(1);
        user.setEmail("test@example.com");

        budgetId = 2;

        budget = new Budget();
        budget.setBudgetId(budgetId);
        budget.setUser(user);
        budget.setCategory(CategoryType.FOOD);
        budget.setBudgetAmount(BigDecimal.valueOf(200));
    }

    @Test
    void save_shouldPersistAndReturnDTO() {
        CreateBudgetDTO dto = new CreateBudgetDTO();
        dto.setCategory(CategoryType.FOOD);
        dto.setBudgetAmount(BigDecimal.valueOf(300));

        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        BudgetResponseDTO result = budgetService.save(user, dto);
        assertEquals(CategoryType.FOOD, result.getCategory());
    }

    @Test
    void getById_shouldReturnDTO() {
        when(budgetRepository.findByBudgetId(budgetId)).thenReturn(budget);

        try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
            utils.when(() -> AuthorizationUtils.validateOwnership(
                    eq(budget.getUser().getUserId()), eq(user.getUserId())
            )).thenCallRealMethod();

            BudgetResponseDTO result = budgetService.getById(user, budgetId);
            assertEquals(CategoryType.FOOD, result.getCategory());
        }
    }

    @Test
    void getById_shouldThrowIfNotFound() {
        when(budgetRepository.findByBudgetId(budgetId)).thenReturn(null);
        assertThrows(BudgetNotFoundException.class, () -> budgetService.getById(user, budgetId));
    }

    @Test
    void getById_shouldThrowIfUnauthorized() {
        when(budgetRepository.findByBudgetId(budgetId)).thenReturn(budget);

        try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
            utils.when(() -> AuthorizationUtils.validateOwnership(
                    any(), any()
            )).thenThrow(new AccessDeniedException("Unauthorized"));

            AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                    budgetService.getById(user, budgetId));

            assertEquals("Unauthorized", ex.getMessage());
        }
    }

    @Test
    void deleteById_shouldDeleteAndReturnMessage() {
        when(budgetRepository.findByBudgetId(budgetId)).thenReturn(budget);

        try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
            utils.when(() -> AuthorizationUtils.validateOwnership(
                    eq(budget.getUser().getUserId()), eq(user.getUserId())
            )).thenCallRealMethod();

            GenericResponse response = budgetService.deleteById(user, budgetId);

            assertNotNull(response);
            assertTrue(response.getMessage().contains("deleted successfully"));
            assertNotNull(response.getTimestamp());
        }
    }


    @Test
    void deleteById_shouldThrowIfNotFound() {
        when(budgetRepository.findByBudgetId(budgetId)).thenReturn(null);
        assertThrows(BudgetNotFoundException.class, () -> budgetService.deleteById(user, budgetId));
    }

    @Test
    void deleteById_shouldThrowIfUnauthorized() {
        when(budgetRepository.findByBudgetId(budgetId)).thenReturn(budget);

        try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
            utils.when(() -> AuthorizationUtils.validateOwnership(
                    any(), any()
            )).thenThrow(new AccessDeniedException("Unauthorized"));

            AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                    budgetService.deleteById(user, budgetId));

            assertEquals("Unauthorized", ex.getMessage());
        }
    }

    @Test
    void update_shouldUpdateFieldsAndReturnDTO() {
        UpdateBudgetDTO dto = new UpdateBudgetDTO();
        dto.setBudgetAmount(BigDecimal.valueOf(500));

        when(budgetRepository.findByBudgetId(budgetId)).thenReturn(budget);
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
            utils.when(() -> AuthorizationUtils.validateOwnership(
                    eq(budget.getUser().getUserId()), eq(user.getUserId())
            )).thenCallRealMethod();

            BudgetResponseDTO result = budgetService.update(user, budgetId, dto);
            assertEquals(BigDecimal.valueOf(500), result.getBudgetAmount());
        }
    }

    @Test
    void update_shouldThrowIfNotFound() {
        when(budgetRepository.findByBudgetId(budgetId)).thenReturn(null);
        assertThrows(BudgetNotFoundException.class, () -> budgetService.update(user, budgetId, new UpdateBudgetDTO()));
    }

    @Test
    void update_shouldThrowIfUnauthorized() {
        when(budgetRepository.findByBudgetId(budgetId)).thenReturn(budget);

        try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
            utils.when(() -> AuthorizationUtils.validateOwnership(
                    any(), any()
            )).thenThrow(new AccessDeniedException("Unauthorized"));

            AccessDeniedException ex = assertThrows(AccessDeniedException.class, () ->
                    budgetService.update(user, budgetId, new UpdateBudgetDTO()));

            assertEquals("Unauthorized", ex.getMessage());
        }
    }

    @Test
    void getBudgetsByUser_shouldReturnMap() {
        when(budgetRepository.findByUser(user)).thenReturn(List.of(budget));

        Map<Integer, BudgetResponseDTO> result = budgetService.getBudgetsByUser(user);
        assertEquals(1, result.size());
    }

    @Test
    void getBudgetsByUser_shouldThrowIfEmpty() {
        when(budgetRepository.findByUser(user)).thenReturn(List.of());
        assertThrows(BudgetNotFoundException.class, () -> budgetService.getBudgetsByUser(user));
    }
}
