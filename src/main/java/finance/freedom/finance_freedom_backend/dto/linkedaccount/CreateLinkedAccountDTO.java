package finance.freedom.finance_freedom_backend.dto.linkedaccount;

import finance.freedom.finance_freedom_backend.enums.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateLinkedAccountDTO {
    @NotBlank(message = "Access token can't be blank")
    private String accessToken;

    @NotBlank(message = "Institution name can't be blank")
    private String institutionName;

    @NotNull(message = "Account type can't be null")
    private AccountType accountType;

    @NotBlank(message = "Account name can't be blank")
    private String accountName;

    private BigDecimal balance = BigDecimal.ZERO;
}
