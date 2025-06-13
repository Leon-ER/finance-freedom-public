package finance.freedom.finance_freedom_backend.util;

import finance.freedom.finance_freedom_backend.interfaces.aws.IAwsSecretsService;
import finance.freedom.finance_freedom_backend.model.aws.EncryptionDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
@Slf4j
public class EncryptionUtil {
    private final IAwsSecretsService awsSecrets;

    public String encrypt(String token) {
        return getEncryptor().encrypt(token);

    }
    public String decrypt(String token) {
        return getEncryptor().decrypt(token);
    }

    private TextEncryptor getEncryptor() {
        EncryptionDetails encryptionDetails = awsSecrets.getEncryptionDetails();
        return Encryptors.delux(encryptionDetails.getPassword(), encryptionDetails.getSalt());
    }
}
