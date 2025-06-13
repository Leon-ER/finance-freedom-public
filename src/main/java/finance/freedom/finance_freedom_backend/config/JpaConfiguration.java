package finance.freedom.finance_freedom_backend.config;

import finance.freedom.finance_freedom_backend.interfaces.aws.IAwsSecretsService;
import finance.freedom.finance_freedom_backend.model.aws.AwsCredentialsRDS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class JpaConfiguration {
    private final IAwsSecretsService awsSecretsService;

    @Bean
    public DataSource dataSource(){
        
        AwsCredentialsRDS credentialsRDS = awsSecretsService.getRdsConnection();


        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        String jdbcUrl = String.format("jdbc:postgresql://%s:%s/%s",
                credentialsRDS.getHost(), credentialsRDS.getPort(), credentialsRDS.getDbname());

        String jdbcDriver = "org.postgresql.Driver";


        dataSource.setDriverClassName(jdbcDriver);
        dataSource.setUsername(credentialsRDS.getUsername());
        dataSource.setPassword(credentialsRDS.getPassword());
        dataSource.setUrl(jdbcUrl);

        try(Connection con = dataSource.getConnection()){
            log.info("Connected to database");
        }catch(SQLException e){
            log.error(e.getMessage());
        }

        return dataSource;

    }
}
