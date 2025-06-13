package finance.freedom.finance_freedom_backend.interfaces.core;

import finance.freedom.finance_freedom_backend.dto.transaction.CreateTransactionDTO;
import finance.freedom.finance_freedom_backend.dto.transaction.TransactionResponseDTO;
import finance.freedom.finance_freedom_backend.dto.transaction.UpdateTransactionDTO;
import finance.freedom.finance_freedom_backend.enums.TransactionType;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public interface ITransactionService {
    TransactionResponseDTO save(User user, CreateTransactionDTO transaction);

    TransactionResponseDTO getTransaction(User user, Integer transactionId);

    GenericResponse deleteTransaction(User user, Integer transactionId);

    TransactionResponseDTO updateTransaction(User user,Integer transactionId,UpdateTransactionDTO updatedTransaction);

    Map<Integer, TransactionResponseDTO> getFilteredTransactions(User user, TransactionType transactionType, LocalDateTime startDate, LocalDateTime endDate);

}
