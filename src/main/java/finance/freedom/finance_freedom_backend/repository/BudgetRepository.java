package finance.freedom.finance_freedom_backend.repository;

import finance.freedom.finance_freedom_backend.model.core.Budget;
import finance.freedom.finance_freedom_backend.model.core.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {

    Budget findByBudgetId(Integer budgetId);

    List<Budget> findByUser(User user);
}
