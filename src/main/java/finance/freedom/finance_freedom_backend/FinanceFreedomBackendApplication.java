package finance.freedom.finance_freedom_backend;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FinanceFreedomBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinanceFreedomBackendApplication.class, args);
	}

}
