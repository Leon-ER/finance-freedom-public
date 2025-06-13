package finance.freedom.finance_freedom_backend.dto.savinggoal;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class SavingGoalResponseDTO {
    private Integer goalId;
    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDateTime targetDate;
    private boolean isCompleted;
    private LocalDateTime completionDate;
}
