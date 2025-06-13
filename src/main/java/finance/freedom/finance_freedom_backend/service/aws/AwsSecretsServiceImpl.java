package finance.freedom.finance_freedom_backend.service.aws;


import finance.freedom.finance_freedom_backend.interfaces.aws.IAwsSecretsService;
import finance.freedom.finance_freedom_backend.model.aws.AwsCredentialsRDS;
import com.google.gson.Gson;
import finance.freedom.finance_freedom_backend.model.aws.EncryptionDetails;
import finance.freedom.finance_freedom_backend.model.aws.JWTSecretKey;
import finance.freedom.finance_freedom_backend.model.aws.JavaMailSenderDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;


@Service
@Slf4j
@RequiredArgsConstructor
public class AwsSecretsServiceImpl implements IAwsSecretsService {

    private final Gson gson;

    @Value("${aws.region}")
    private String awsRegion;

    public String getSecret(String secretName) {
        SecretsManagerClient client = SecretsManagerClient.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        GetSecretValueRequest getSecretValueRequest = GetSecretValueRequest.builder()
                .secretId(secretName)
                .build();

        GetSecretValueResponse getSecretValueResponse;

        getSecretValueResponse = client.getSecretValue(getSecretValueRequest);

        return getSecretValueResponse.secretString();
    }

    @Cacheable(value = "aws-secret", key = "'rds'")
    public AwsCredentialsRDS getRdsConnection() {
        String secretName = "RDS-connection-credentials-postgres";

        String secret = getSecret(secretName);

        return gson.fromJson(secret, AwsCredentialsRDS.class);
    }

    @Cacheable(value = "aws-secret", key = "'jwt'")
    public JWTSecretKey getSecretKey() {
        String secretName = "jwt-secret-key";

        String secret = getSecret(secretName);

        return gson.fromJson(secret, JWTSecretKey.class);
    }

    @Cacheable(value = "aws-secret", key = "'mail'")
    public JavaMailSenderDetails getJavaMailSenderInfo() {
        String secretName = "java-mail-sender-info";

        String secret = getSecret(secretName);

        return gson.fromJson(secret, JavaMailSenderDetails.class);
    }
    @Cacheable(value = "aws-secret", key = "'encryption'")
    @Override
    public EncryptionDetails getEncryptionDetails() {
        String secretName = "Encryption-data";

        String secret = getSecret(secretName);

        return gson.fromJson(secret, EncryptionDetails.class);
    }

}
