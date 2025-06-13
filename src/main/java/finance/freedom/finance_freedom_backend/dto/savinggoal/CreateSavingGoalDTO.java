package finance.freedom.finance_freedom_backend.dto.savinggoal;

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
@NoArgsConstructor
@AllArgsConstructor
public class CreateSavingGoalDTO {

    @NotBlank(message = "Goal name cant be blank")
    private String goalName;

    @NotNull(message = "Target amount cant be null")
    private BigDecimal targetAmount;

    private BigDecimal currentAmount;
    private LocalDateTime targetDate;
    private boolean isCompleted = false;
    private LocalDateTime completionDate;
}
