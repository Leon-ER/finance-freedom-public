package finance.freedom.finance_freedom_backend.dto.budget;

import finance.freedom.finance_freedom_backend.enums.CategoryType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateBudgetDTO {

    @NotNull(message = "Category cant be blank")
    private CategoryType category;

    @NotNull(message = "budget cant be null")
    private BigDecimal budgetAmount;

    @NotNull(message = "date cant be null")
    private LocalDateTime startDate;

    @NotNull(message = "date cant be null")
    private LocalDateTime endDate;

    @NotNull(message = "notify threshold percent cant be null")
    private BigDecimal notifyThresholdPercentage;
}
