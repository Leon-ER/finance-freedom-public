package finance.freedom.finance_freedom_backend.model.aws;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AwsCredentialsRDS {
    private String username, password , engine, host,port, dbname, dbInstanceIdentifier;
}


