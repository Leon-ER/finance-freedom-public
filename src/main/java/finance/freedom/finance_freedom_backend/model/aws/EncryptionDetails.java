package finance.freedom.finance_freedom_backend.model.aws;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EncryptionDetails {
    private String password;
    private String salt;
}
