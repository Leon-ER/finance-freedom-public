package finance.freedom.finance_freedom_backend.model.aws;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JavaMailSenderDetails {
    private String smtpUsername, smtpPassword, smtpHost, fromName, fromEmail;
    private int smtpPort;
}
