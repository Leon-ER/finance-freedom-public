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
public class UpdateLinkedAccountDTO {
    private String accessToken;

    private String institutionName;

    private AccountType accountType;

    private String accountName;

    private BigDecimal balance = BigDecimal.ZERO;
}
