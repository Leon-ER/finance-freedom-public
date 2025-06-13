package finance.freedom.finance_freedom_backend.repository;

import finance.freedom.finance_freedom_backend.model.core.SavingGoal;
import finance.freedom.finance_freedom_backend.model.core.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SavingGoalRepository extends JpaRepository<SavingGoal, UUID> {
    List<SavingGoal> findByUser(User user);

    SavingGoal findByGoalId(Integer savingGoalId);

}
