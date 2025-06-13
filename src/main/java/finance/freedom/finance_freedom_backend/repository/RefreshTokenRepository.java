package finance.freedom.finance_freedom_backend.repository;

import finance.freedom.finance_freedom_backend.model.core.RefreshToken;
import finance.freedom.finance_freedom_backend.model.core.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {
    RefreshToken findByRefreshToken(String refreshToken);

    @Modifying
    @Transactional
    void deleteAllByUser(User user);
}
