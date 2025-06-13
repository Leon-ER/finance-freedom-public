package finance.freedom.finance_freedom_backend.dto.linkedaccount;

import finance.freedom.finance_freedom_backend.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LinkedAccountResponseDTO {
    private Integer accountId;
    private String accountName;
    private AccountType accountType;
    private BigDecimal balance;
}
