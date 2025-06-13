package finance.freedom.finance_freedom_backend.model.core;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "savings_goals")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SavingGoal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "goal_id")
    private Integer goalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "goal_name", nullable = false, length = 100)
    private String goalName;

    @Column(name = "target_amount", nullable = false)
    @Check(constraints = "target_amount >= 0")
    private BigDecimal targetAmount;

    @Column(name = "current_amount", nullable = true, columnDefinition = "decimal(10,2) default 0")
    private BigDecimal currentAmount = BigDecimal.ZERO;

    @Column(name = "target_date", nullable = true)
    private LocalDateTime targetDate;

    @Column(name = "is_completed", nullable = true)
    private boolean isCompleted = false;

    @Column(name = "completion_date", nullable = true)
    private LocalDateTime completionDate;
}
