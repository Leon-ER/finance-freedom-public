package finance.freedom.finance_freedom_backend.service.core;

import finance.freedom.finance_freedom_backend.dto.budget.BudgetResponseDTO;
import finance.freedom.finance_freedom_backend.dto.budget.CreateBudgetDTO;
import finance.freedom.finance_freedom_backend.dto.budget.UpdateBudgetDTO;
import finance.freedom.finance_freedom_backend.exception.customexceptions.BudgetNotFoundException;
import finance.freedom.finance_freedom_backend.interfaces.core.IBudgetService;
import finance.freedom.finance_freedom_backend.model.core.Budget;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.repository.BudgetRepository;
import finance.freedom.finance_freedom_backend.util.AuthorizationUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetServiceImpl implements IBudgetService {

    private final BudgetRepository budgetRepository;


    @Override
    public BudgetResponseDTO save(User user, CreateBudgetDTO createBudgetDTO) {
        log.info("Attempting to save budget for user {}", user.getEmail());
        Budget budget = new Budget();

        budget.setBudgetAmount(createBudgetDTO.getBudgetAmount());
        budget.setCategory(createBudgetDTO.getCategory());
        budget.setEndDate(createBudgetDTO.getEndDate());
        budget.setStartDate(createBudgetDTO.getStartDate());
        budget.setNotifyThresholdPercentage(createBudgetDTO.getNotifyThresholdPercentage());
        budget.setUser(user);

        budgetRepository.save(budget);
        log.info("Budget saved successfully {}", budget.getBudgetId());
        return createDTO(budget);
    }

    @Override
    public BudgetResponseDTO getById(User user, Integer budgetId) {
        log.info("Attempting to get budget by id {}", budgetId);
        Budget budget = budgetRepository.findByBudgetId(budgetId);

        if (budget == null) {
            log.warn("Budget not found for id: {}", budgetId);
            throw new BudgetNotFoundException("Budget not found");
        }
        AuthorizationUtils.validateOwnership(budget.getUser().getUserId(), user.getUserId());
        log.info("Budget found successfully {}", budget.getBudgetId());
        return createDTO(budget);
    }

    @Override
    public GenericResponse deleteById(User user, Integer budgetId) {
        log.info("Attempting to delete budget by id {}", budgetId);
        Budget budget = budgetRepository.findByBudgetId(budgetId);

        if (budget == null) {
            log.warn("Budget not found for id: {}", budgetId);
            throw new BudgetNotFoundException("Budget not found");
        }

        AuthorizationUtils.validateOwnership(budget.getUser().getUserId(), user.getUserId());

        budgetRepository.delete(budget);

        log.info("Budget deleted successfully {}", budgetId);

        return new GenericResponse(String.format("Budget with id: %s deleted successfully", budgetId));
    }


    @Override
    public BudgetResponseDTO update(User user, Integer budgetId, UpdateBudgetDTO updateBudgetDTO) {
        log.info("Attempting to update budget by id {}", budgetId);
        Budget budget = budgetRepository.findByBudgetId(budgetId);

        if (budget == null) {
            log.warn("Budget not found for id: {}", budgetId);
            throw new BudgetNotFoundException("Budget not found");
        }

        AuthorizationUtils.validateOwnership(budget.getUser().getUserId(), user.getUserId());

        if(updateBudgetDTO.getBudgetAmount() != null) {
            budget.setBudgetAmount(updateBudgetDTO.getBudgetAmount());
        }

        if(updateBudgetDTO.getCategory() != null) {
            budget.setCategory(updateBudgetDTO.getCategory());
        }
        if(updateBudgetDTO.getEndDate() != null) {
            budget.setEndDate(updateBudgetDTO.getEndDate());
        }
        if(updateBudgetDTO.getStartDate() != null) {
            budget.setStartDate(updateBudgetDTO.getStartDate());
        }
        if(updateBudgetDTO.getNotifyThresholdPercentage() != null) {
            budget.setNotifyThresholdPercentage(updateBudgetDTO.getNotifyThresholdPercentage());
        }

        budgetRepository.save(budget);

        log.info("Budget updated successfully {}", budget.getBudgetId());

        return createDTO(budget);
    }

    @Cacheable(value = "budget", key = "#user.userId")
    @Override
    public Map<Integer, BudgetResponseDTO> getBudgetsByUser(User user) {
        log.info("Attempting to get budgets for user {}", user.getEmail());
        List<Budget> budgets = budgetRepository.findByUser(user);

        if(budgets.isEmpty()) {
            log.warn("Budgets for user not found");
            throw new BudgetNotFoundException("Budgets for user not found");
        }

        Map<Integer, BudgetResponseDTO> result = new HashMap<>();

        for(Budget budget : budgets) {
            result.put(budget.getBudgetId(), createDTO(budget));
        }
        log.info("Budgets found successfully");
        return result;
    }


    public BudgetResponseDTO createDTO(Budget budget) {
        BudgetResponseDTO budgetResponseDTO = new BudgetResponseDTO();

        budgetResponseDTO.setBudgetId(budget.getBudgetId());
        budgetResponseDTO.setBudgetAmount(budget.getBudgetAmount());
        budgetResponseDTO.setCategory(budget.getCategory());
        budgetResponseDTO.setEndDate(budget.getEndDate());
        budgetResponseDTO.setStartDate(budget.getStartDate());
        budgetResponseDTO.setNotifyThresholdPercentage(budget.getNotifyThresholdPercentage());

        return budgetResponseDTO;
    }
}
