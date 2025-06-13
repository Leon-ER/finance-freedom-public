package finance.freedom.finance_freedom_backend.dto.transaction;

import finance.freedom.finance_freedom_backend.enums.CategoryType;
import finance.freedom.finance_freedom_backend.enums.RecurrenceInterval;
import finance.freedom.finance_freedom_backend.enums.TransactionType;
import finance.freedom.finance_freedom_backend.model.core.User;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateTransactionDTO {

    @NotNull(message = "Amount can't be null")
    private BigDecimal amount;

    @NotNull(message = "Category can't be blank")
    private CategoryType category;

    @NotBlank(message = "Description can't be blank")
    private String description;

    @NotNull(message = "Transaction type can't be null")
    private TransactionType transactionType;

    private LocalDateTime transactionDate;

    private boolean isRecurring = false;

    private RecurrenceInterval recurrenceInterval;
}
