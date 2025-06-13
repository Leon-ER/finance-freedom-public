package finance.freedom.finance_freedom_backend.repository;

import finance.freedom.finance_freedom_backend.model.core.LinkedAccount;
import finance.freedom.finance_freedom_backend.model.core.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LinkedAccountRepository extends JpaRepository<LinkedAccount, UUID> {
    List<LinkedAccount> findByUser(User user);
    LinkedAccount findByAccountId(Integer linkedAccountId);
}
