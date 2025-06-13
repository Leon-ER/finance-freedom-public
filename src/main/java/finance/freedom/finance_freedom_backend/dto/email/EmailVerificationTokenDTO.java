package finance.freedom.finance_freedom_backend.dto.email;

import finance.freedom.finance_freedom_backend.enums.TokenPurpose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerificationTokenDTO {
    private String token;
    private TokenPurpose purpose;
    private LocalDateTime expiresAt;
}
