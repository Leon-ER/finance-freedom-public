package finance.freedom.finance_freedom_backend.service.core;

import finance.freedom.finance_freedom_backend.dto.transaction.CreateTransactionDTO;
import finance.freedom.finance_freedom_backend.dto.transaction.TransactionResponseDTO;
import finance.freedom.finance_freedom_backend.dto.transaction.UpdateTransactionDTO;
import finance.freedom.finance_freedom_backend.enums.RecurrenceInterval;
import finance.freedom.finance_freedom_backend.enums.TransactionType;
import finance.freedom.finance_freedom_backend.exception.customexceptions.TransactionNotFoundException;
import finance.freedom.finance_freedom_backend.interfaces.core.ITransactionService;
import finance.freedom.finance_freedom_backend.model.core.Transaction;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.repository.TransactionRepository;
import finance.freedom.finance_freedom_backend.util.AuthorizationUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class TransactionServiceImpl implements ITransactionService {

    private final TransactionRepository transactionRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public TransactionResponseDTO save(User user, CreateTransactionDTO transaction) {

        log.info("Attempting to save transaction for user {}", user.getEmail());

        Transaction transactionEntity = new Transaction();

        if (transaction.isRecurring() && transaction.getRecurrenceInterval() == null ) {
            log.warn("Recurrence interval is required when recurring transaction is true");
            throw new IllegalArgumentException("Recurrence interval is required when recurring transaction is true");
        }

        LocalDateTime date = (transaction.getTransactionDate() == null)
                ? LocalDateTime.now()
                : transaction.getTransactionDate();

        RecurrenceInterval interval = (transaction.getRecurrenceInterval() ==null)
                ? RecurrenceInterval.NONE
                : transaction.getRecurrenceInterval();

        transactionEntity.setRecurrenceInterval(interval);
        transactionEntity.setAmount(transaction.getAmount());
        transactionEntity.setCategory(transaction.getCategory());
        transactionEntity.setDescription(transaction.getDescription());
        transactionEntity.setTransactionType(transaction.getTransactionType());
        transactionEntity.setTransactionDate(date);
        transactionEntity.setUser(user);
        transactionEntity.setRecurring(transaction.isRecurring());

        transactionRepository.save(transactionEntity);

        log.info("Transaction saved successfully {}", transactionEntity.getTransactionId());

        return createDTO(transactionEntity);
    }

    @Override
    public TransactionResponseDTO getTransaction(User user,Integer transactionId) {

        log.info("Attempting to get transaction by id {}", transactionId);

        Transaction transaction = transactionRepository.findByTransactionId(transactionId);
        if (transaction == null) {
            log.warn("Transaction not found for id: {}", transactionId);
            throw new TransactionNotFoundException(String.format("Transaction with id %s not found",transactionId.toString()));
        }

        AuthorizationUtils.validateOwnership(transaction.getUser().getUserId(), user.getUserId());

        log.info("Transaction found successfully {}", transaction.getTransactionId());

        return createDTO(transaction);
    }

    @Override
    @Cacheable(
            value = "transaction",
            key = "T(String).format('%s-%s-%s-%s', #user.userId, #transactionType != null ? #transactionType.name() : 'null', #startDate != null ? #startDate.toString() : 'null', #endDate != null ? #endDate.toString() : 'null')"
    )
    public Map<Integer, TransactionResponseDTO> getFilteredTransactions(User user, TransactionType transactionType, LocalDateTime startDate, LocalDateTime endDate) {

        //Todo add pagination

        log.info("Attempting to get filtered transactions for user {}", user.getEmail());
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Transaction> query = builder.createQuery(Transaction.class);
        Root<Transaction> root = query.from(Transaction.class);

        List<Predicate> predicates = new ArrayList<>();

        predicates.add(builder.equal(root.get("user").get("userId"), user.getUserId()));

        if (transactionType != null) {
            predicates.add(builder.equal(root.get("transactionType"), transactionType));
        }

        if (startDate != null) {
            predicates.add(builder.greaterThanOrEqualTo(root.get("transactionDate"), Timestamp.valueOf(startDate)));
        }

        if (endDate != null) {
            predicates.add(builder.lessThanOrEqualTo(root.get("transactionDate"), Timestamp.valueOf(endDate)));
        }

        query.where(builder.and(predicates.toArray(new Predicate[0])));
        query.orderBy(builder.desc(root.get("transactionDate")));

        List<Transaction> transactionsQueried = entityManager.createQuery(query).getResultList();

        if (transactionsQueried.isEmpty()) {
            log.warn("No transactions found for user {}", user.getEmail());
            throw new TransactionNotFoundException(String.format("No transactions found for user %s",user.getEmail()));
        }

        Map<Integer,TransactionResponseDTO> result = new HashMap<>();

        for (Transaction transaction : transactionsQueried) {
            result.put(transaction.getTransactionId() ,createDTO(transaction));

        }
        log.info("Transactions found successfully");
        return result;
    }

    public GenericResponse deleteTransaction(User user, Integer transactionId) {
        log.info("Attempting to delete transaction by id {}", transactionId);

        Transaction transaction = transactionRepository.findByTransactionId(transactionId);
        if (transaction == null) {
            log.warn("Transaction not found for id: {}", transactionId);
            throw new TransactionNotFoundException(String.format("Transaction with id %s not found",transactionId.toString()));
        }
        AuthorizationUtils.validateOwnership(transaction.getUser().getUserId(), user.getUserId());

        transactionRepository.delete(transaction);

        log.info("Transaction deleted successfully {}", transactionId);
        return new GenericResponse(String.format("Transaction with id: %s deleted successfully",transactionId));
    }

    @Override
    public TransactionResponseDTO updateTransaction(User user,Integer transactionId,UpdateTransactionDTO updatedTransaction) {

        log.info("Attempting to update transaction by id {}", transactionId);

        Transaction transaction = transactionRepository.findByTransactionId(transactionId);
        if (transaction == null) {
            log.warn("Transaction not found for id: {}", transactionId);
            throw new TransactionNotFoundException(String.format("Transaction with id %s not found",transactionId));
        }

        AuthorizationUtils.validateOwnership(transaction.getUser().getUserId(), user.getUserId());


        if(updatedTransaction.getAmount() != null) {
            transaction.setAmount(updatedTransaction.getAmount());
        }
        if(updatedTransaction.getCategory() != null) {
            transaction.setCategory(updatedTransaction.getCategory());
        }
        if(updatedTransaction.getDescription() != null) {
            transaction.setDescription(updatedTransaction.getDescription());
        }
        if(updatedTransaction.getTransactionDate() != null) {
            transaction.setTransactionDate(updatedTransaction.getTransactionDate());
        }
        if(updatedTransaction.getTransactionType() != null) {
            transaction.setTransactionType(updatedTransaction.getTransactionType());
        }
        if(updatedTransaction.getRecurrenceInterval() != null){
            transaction.setRecurrenceInterval(updatedTransaction.getRecurrenceInterval());
        }

        transactionRepository.save(transaction);

        log.info("Transaction updated successfully {}", transactionId);

        return createDTO(transaction);
    }


    private TransactionResponseDTO createDTO(Transaction transaction) {
        TransactionResponseDTO transactionResponseDTO = new TransactionResponseDTO();

        transactionResponseDTO.setTransactionId(transaction.getTransactionId());
        transactionResponseDTO.setAmount(transaction.getAmount());
        transactionResponseDTO.setCategory(transaction.getCategory());
        transactionResponseDTO.setDescription(transaction.getDescription());
        transactionResponseDTO.setTransactionDate(transaction.getTransactionDate());
        transactionResponseDTO.setRecurring(transaction.isRecurring());
        transactionResponseDTO.setRecurrenceInterval(transaction.getRecurrenceInterval());
        transactionResponseDTO.setTransactionType(transaction.getTransactionType());

        return transactionResponseDTO;
    }

}
