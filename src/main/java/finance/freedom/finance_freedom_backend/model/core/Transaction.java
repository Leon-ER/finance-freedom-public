package finance.freedom.finance_freedom_backend.model.core;

import finance.freedom.finance_freedom_backend.enums.CategoryType;
import finance.freedom.finance_freedom_backend.enums.RecurrenceInterval;
import finance.freedom.finance_freedom_backend.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import java.math.BigDecimal;
import java.time.LocalDateTime;


@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Integer transactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50, nullable = false)
    private CategoryType category;

    @Column(name = "description", length = 500, nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "is_recurring", nullable = true)
    private boolean isRecurring = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_interval", nullable = true)
    private RecurrenceInterval recurrenceInterval;

}
