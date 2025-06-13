package finance.freedom.finance_freedom_backend.interfaces.aws;

import finance.freedom.finance_freedom_backend.model.aws.AwsCredentialsRDS;
import finance.freedom.finance_freedom_backend.model.aws.EncryptionDetails;
import finance.freedom.finance_freedom_backend.model.aws.JWTSecretKey;
import finance.freedom.finance_freedom_backend.model.aws.JavaMailSenderDetails;

public interface IAwsSecretsService {

    AwsCredentialsRDS getRdsConnection();

    JWTSecretKey getSecretKey();

    JavaMailSenderDetails getJavaMailSenderInfo();

    EncryptionDetails getEncryptionDetails();
}
