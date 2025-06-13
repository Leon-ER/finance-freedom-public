package finance.freedom.finance_freedom_backend.repository;

import finance.freedom.finance_freedom_backend.model.core.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface UserRepository extends JpaRepository<User, UUID> {

    User findByEmail(String email);
}
