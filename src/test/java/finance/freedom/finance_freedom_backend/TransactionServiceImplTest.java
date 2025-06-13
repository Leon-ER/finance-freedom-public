package finance.freedom.finance_freedom_backend;

import finance.freedom.finance_freedom_backend.dto.transaction.*;
import finance.freedom.finance_freedom_backend.enums.CategoryType;
import finance.freedom.finance_freedom_backend.enums.RecurrenceInterval;
import finance.freedom.finance_freedom_backend.enums.TransactionType;
import finance.freedom.finance_freedom_backend.exception.customexceptions.AccessDeniedException;
import finance.freedom.finance_freedom_backend.exception.customexceptions.TransactionNotFoundException;
import finance.freedom.finance_freedom_backend.model.core.Transaction;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.repository.TransactionRepository;
import finance.freedom.finance_freedom_backend.service.core.TransactionServiceImpl;
import jakarta.persistence.*;
import jakarta.persistence.criteria.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import finance.freedom.finance_freedom_backend.util.AuthorizationUtils;
import org.mockito.MockedStatic;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceImplTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private EntityManager entityManager;
    @InjectMocks private TransactionServiceImpl transactionService;

    private User user;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setUserId(1);
        user.setEmail("test@example.com");

        Field emField = TransactionServiceImpl.class.getDeclaredField("entityManager");
        emField.setAccessible(true);
        emField.set(transactionService, entityManager);
    }

    @Test
    void getTransaction_shouldThrowIfUnauthorized() {
        Integer id = 2;
        Transaction tx = new Transaction();
        tx.setTransactionId(id);
        tx.setUser(user);

        when(transactionRepository.findByTransactionId(id)).thenReturn(tx);

        try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
            utils.when(() -> AuthorizationUtils.validateOwnership(any(), any()))
                    .thenThrow(new AccessDeniedException("Unauthorized"));

            RuntimeException ex = assertThrows(RuntimeException.class, () ->
                    transactionService.getTransaction(user, id));

            assertEquals("Unauthorized", ex.getMessage());
        }
    }

    @Test
    void save_shouldPersistTransactionAndReturnDTO() {
        CreateTransactionDTO dto = new CreateTransactionDTO();
        dto.setAmount(BigDecimal.valueOf(100));
        dto.setCategory(CategoryType.FOOD);
        dto.setDescription("Lunch");
        dto.setTransactionType(TransactionType.EXPENSE);
        dto.setRecurring(true);
        dto.setRecurrenceInterval(RecurrenceInterval.MONTHLY);
        dto.setTransactionDate(LocalDateTime.now());

        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction saved = invocation.getArgument(0);
            saved.setTransactionId(100);
            return saved;
        });

        TransactionResponseDTO result = transactionService.save(user, dto);

        assertNotNull(result.getTransactionId());
        assertEquals(100, result.getTransactionId());
    }

    @Test
    void save_shouldThrowWhenRecurringWithoutInterval() {
        CreateTransactionDTO dto = new CreateTransactionDTO();
        dto.setRecurring(true);
        dto.setTransactionDate(LocalDateTime.now());

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> transactionService.save(user, dto));

        assertEquals("Recurrence interval is required when recurring transaction is true", ex.getMessage());
    }

    @Test
    void getTransaction_shouldReturnDTOIfFound() {
        Integer id = 10;
        Transaction tx = new Transaction();
        tx.setTransactionId(id);
        tx.setAmount(BigDecimal.TEN);
        tx.setCategory(CategoryType.GROCERIES);
        tx.setDescription("Shopping");
        tx.setTransactionType(TransactionType.EXPENSE);
        tx.setTransactionDate(LocalDateTime.now());
        tx.setUser(user);

        when(transactionRepository.findByTransactionId(id)).thenReturn(tx);

        try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
            utils.when(() -> AuthorizationUtils.validateOwnership(
                    eq(tx.getUser().getUserId()), eq(user.getUserId())
            )).thenCallRealMethod();

            TransactionResponseDTO result = transactionService.getTransaction(user, id);

            assertNotNull(result);
            assertEquals(CategoryType.GROCERIES, result.getCategory());
        }
    }

    @Test
    void getTransaction_shouldThrowIfNotFound() {
        Integer id = 999;
        when(transactionRepository.findByTransactionId(id)).thenReturn(null);
        assertThrows(TransactionNotFoundException.class, () -> transactionService.getTransaction(user, id));
    }

    @Test
    void deleteTransaction_shouldDeleteIfExists() {
        Integer id = 20;
        Transaction tx = new Transaction();
        tx.setTransactionId(id);
        tx.setUser(user);

        when(transactionRepository.findByTransactionId(id)).thenReturn(tx);

        try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
            utils.when(() -> AuthorizationUtils.validateOwnership(
                    eq(tx.getUser().getUserId()), eq(user.getUserId())
            )).thenCallRealMethod();

            GenericResponse response = transactionService.deleteTransaction(user, id);

            assertNotNull(response);
            assertTrue(response.getMessage().contains("deleted successfully"));
            assertNotNull(response.getTimestamp());
            verify(transactionRepository).delete(tx);
        }
    }

    @Test
    void deleteTransaction_shouldThrowIfNotFound() {
        Integer id = 999;
        when(transactionRepository.findByTransactionId(id)).thenReturn(null);
        assertThrows(TransactionNotFoundException.class, () -> transactionService.deleteTransaction(user, id));
    }

    @Test
    void updateTransaction_shouldUpdateAndReturnDTO() {
        Integer id = 123;
        Transaction tx = new Transaction();
        tx.setTransactionId(id);
        tx.setUser(user);

        UpdateTransactionDTO dto = new UpdateTransactionDTO();
        dto.setAmount(BigDecimal.valueOf(50));
        dto.setCategory(CategoryType.GROCERIES);

        when(transactionRepository.findByTransactionId(id)).thenReturn(tx);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(tx);

        try (MockedStatic<AuthorizationUtils> utils = mockStatic(AuthorizationUtils.class)) {
            utils.when(() -> AuthorizationUtils.validateOwnership(
                    eq(tx.getUser().getUserId()), eq(user.getUserId())
            )).thenCallRealMethod();

            TransactionResponseDTO result = transactionService.updateTransaction(user, id, dto);

            assertNotNull(result);
            assertEquals(CategoryType.GROCERIES, result.getCategory());
        }
    }

    @Test
    void getFilteredTransactions_shouldReturnFilteredMap() {
        LocalDateTime start = LocalDateTime.now().minusDays(5);
        LocalDateTime end = LocalDateTime.now();

        CriteriaBuilder builder = mock(CriteriaBuilder.class);
        CriteriaQuery<Transaction> query = mock(CriteriaQuery.class);
        Root<Transaction> root = mock(Root.class);
        Path<Object> userPath = mock(Path.class);
        Path<Object> userIdPath = mock(Path.class);
        TypedQuery<Transaction> typedQuery = mock(TypedQuery.class);

        Transaction tx = new Transaction();
        tx.setTransactionId(321);
        tx.setUser(user);
        tx.setAmount(BigDecimal.TEN);
        tx.setCategory(CategoryType.FUN);
        tx.setDescription("Test desc");
        tx.setTransactionType(TransactionType.EXPENSE);
        tx.setTransactionDate(LocalDateTime.now());

        List<Transaction> transactions = List.of(tx);

        when(entityManager.getCriteriaBuilder()).thenReturn(builder);
        when(builder.createQuery(Transaction.class)).thenReturn(query);
        when(query.from(Transaction.class)).thenReturn(root);
        when(entityManager.createQuery(query)).thenReturn(typedQuery);
        when(typedQuery.getResultList()).thenReturn(transactions);

        when(root.get("user")).thenReturn(userPath);
        when(userPath.get("userId")).thenReturn(userIdPath);
        when(builder.equal(userIdPath, user.getUserId())).thenReturn(mock(Predicate.class));
        when(builder.and(any(Predicate[].class))).thenReturn(mock(Predicate.class));
        when(query.orderBy((Order) any())).thenReturn(query);

        Map<Integer, TransactionResponseDTO> result = transactionService.getFilteredTransactions(user, null, start, end);

        assertEquals(1, result.size());
    }
}
