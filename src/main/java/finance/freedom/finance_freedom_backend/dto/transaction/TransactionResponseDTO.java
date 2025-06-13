package finance.freedom.finance_freedom_backend.dto.transaction;

import finance.freedom.finance_freedom_backend.enums.CategoryType;
import finance.freedom.finance_freedom_backend.enums.RecurrenceInterval;
import finance.freedom.finance_freedom_backend.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponseDTO {
    private Integer transactionId;
    private BigDecimal amount;
    private CategoryType category;
    private String description;
    private TransactionType transactionType;
    private LocalDateTime transactionDate;
    private boolean isRecurring;
    private RecurrenceInterval recurrenceInterval;
}
