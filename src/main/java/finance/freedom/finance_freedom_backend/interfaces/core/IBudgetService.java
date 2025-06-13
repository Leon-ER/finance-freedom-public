package finance.freedom.finance_freedom_backend.interfaces.core;

import finance.freedom.finance_freedom_backend.dto.budget.BudgetResponseDTO;
import finance.freedom.finance_freedom_backend.dto.budget.CreateBudgetDTO;
import finance.freedom.finance_freedom_backend.dto.budget.UpdateBudgetDTO;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;

import java.util.Map;
import java.util.UUID;

public interface IBudgetService {
    BudgetResponseDTO save(User user, CreateBudgetDTO createBudgetDTO);

    BudgetResponseDTO getById(User user,Integer budgetId);

    GenericResponse deleteById(User user, Integer budgetId);

    BudgetResponseDTO update(User user, Integer budgetId, UpdateBudgetDTO createBudgetDTO);

    Map<Integer, BudgetResponseDTO> getBudgetsByUser(User user);
}
