package finance.freedom.finance_freedom_backend.dto.savinggoal;

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
public class UpdateSavingGoalDTO {

    private String goalName;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDateTime targetDate;
    private boolean isCompleted;
    private LocalDateTime completionDate;
}
