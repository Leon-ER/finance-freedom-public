package finance.freedom.finance_freedom_backend.dto.budget;

import finance.freedom.finance_freedom_backend.enums.CategoryType;
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
public class UpdateBudgetDTO {
    private CategoryType category;
    private BigDecimal budgetAmount;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal notifyThresholdPercentage;
}
