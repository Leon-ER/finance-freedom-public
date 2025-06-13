package finance.freedom.finance_freedom_backend.service.core;

import finance.freedom.finance_freedom_backend.dto.savinggoal.CreateSavingGoalDTO;
import finance.freedom.finance_freedom_backend.dto.savinggoal.SavingGoalResponseDTO;
import finance.freedom.finance_freedom_backend.dto.savinggoal.UpdateSavingGoalDTO;
import finance.freedom.finance_freedom_backend.exception.customexceptions.SavingGoalNotFoundException;
import finance.freedom.finance_freedom_backend.interfaces.core.ISavingGoalService;
import finance.freedom.finance_freedom_backend.model.core.SavingGoal;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.repository.SavingGoalRepository;
import finance.freedom.finance_freedom_backend.util.AuthorizationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class SavingGoalServiceImpl implements ISavingGoalService {

    private final SavingGoalRepository savingGoalRepository;

    @Override
    public SavingGoalResponseDTO save(User user, CreateSavingGoalDTO savingGoal) {
        log.info("Attempting to save saving goal for user {}", user.getEmail());

        SavingGoal goal = new SavingGoal();

        goal.setGoalName(savingGoal.getGoalName());
        goal.setUser(user);
        goal.setCompleted(savingGoal.isCompleted());
        goal.setCurrentAmount(savingGoal.getCurrentAmount());
        goal.setTargetAmount(savingGoal.getTargetAmount());
        goal.setCompletionDate(savingGoal.getCompletionDate());
        goal.setTargetDate(savingGoal.getTargetDate());

        savingGoalRepository.save(goal);

        log.info("Saving goal saved successfully {}", goal.getGoalId());
        return createDTO(goal);
    }

    @Override
    public SavingGoalResponseDTO getSavingGoalById(User user,Integer savingGoalId) {

        log.info("Attempting to get saving goal by id {}", savingGoalId);

        SavingGoal savingGoal = savingGoalRepository.findByGoalId(savingGoalId);

        if (savingGoal == null) {
            log.warn("Saving goal not found for id: {}", savingGoalId);
            throw new SavingGoalNotFoundException("Saving goal not found");
        }

        AuthorizationUtils.validateOwnership(savingGoal.getUser().getUserId(), user.getUserId());

        log.info("Saving goal found successfully {}", savingGoal.getGoalId());
        return createDTO(savingGoal);
    }

    @Cacheable(value = "savingGoal", key = "#user.userId")
    @Override
    public Map<Integer,SavingGoalResponseDTO> getSavingGoal(User user) {
        log.info("Attempting to get saving goals for user {}", user.getEmail());

        List<SavingGoal> savingGoals = savingGoalRepository.findByUser(user);

        if (savingGoals.isEmpty()) {
            log.warn("Saving goals for user not found");
            throw new SavingGoalNotFoundException("Saving goal for user not found");
        }

        Map<Integer,SavingGoalResponseDTO> result = new HashMap<>();

        for(SavingGoal savingGoal : savingGoals) {
            result.put(savingGoal.getGoalId(), createDTO(savingGoal));
        }

        log.info("Saving goals found successfully");
        return result;
    }

    @Override
    public SavingGoalResponseDTO updateSavingGoal(User user,Integer savingGoalId, UpdateSavingGoalDTO savingGoal) {
        log.info("Attempting to update saving goal by id {}", savingGoalId);

        SavingGoal goal = savingGoalRepository.findByGoalId(savingGoalId);

        if (goal == null) {
            log.warn("Saving goal not found for id: {}", savingGoalId);
            throw new SavingGoalNotFoundException("Saving goal not found");
        }

        AuthorizationUtils.validateOwnership(goal.getUser().getUserId(), user.getUserId());


        if(savingGoal.getGoalName() != null) {
            goal.setGoalName(savingGoal.getGoalName());
        }
        if(savingGoal.getCurrentAmount() != null) {
            goal.setCurrentAmount(savingGoal.getCurrentAmount());
        }
        if(savingGoal.getTargetAmount() != null) {
            goal.setTargetAmount(savingGoal.getTargetAmount());
        }
        if(savingGoal.getCompletionDate() != null) {
            goal.setCompletionDate(savingGoal.getCompletionDate());
        }
        if(savingGoal.isCompleted()){
            goal.setCompleted(true);
        }
        if(savingGoal.getTargetDate() != null) {
            goal.setTargetDate(savingGoal.getTargetDate());
        }

        savingGoalRepository.save(goal);

        log.info("Saving goal updated successfully {}", savingGoalId);

        return createDTO(goal);
    }

    @Transactional
    @Override
    public GenericResponse deleteSavingGoal(User user, Integer savingGoalId) {

        log.info("Attempting to delete saving goal by id {}", savingGoalId);

        SavingGoal goal = savingGoalRepository.findByGoalId(savingGoalId);
        if (goal == null) {
            log.warn("Saving goal not found for id: {}", savingGoalId);
            throw new SavingGoalNotFoundException("Saving goal not found");
        }
        AuthorizationUtils.validateOwnership(goal.getUser().getUserId(), user.getUserId());

        savingGoalRepository.delete(goal);

        log.info("Saving goal deleted successfully {}", savingGoalId);

        return new GenericResponse(String.format("Saving goal with id: %s deleted successfully",savingGoalId));
    }

    public SavingGoalResponseDTO createDTO(SavingGoal savingGoal) {
        SavingGoalResponseDTO savingGoalResponseDTO = new SavingGoalResponseDTO();

        savingGoalResponseDTO.setGoalId(savingGoal.getGoalId());
        savingGoalResponseDTO.setCompleted(savingGoal.isCompleted());
        savingGoalResponseDTO.setGoalName(savingGoal.getGoalName());
        savingGoalResponseDTO.setCurrentAmount(savingGoal.getCurrentAmount());
        savingGoalResponseDTO.setTargetAmount(savingGoal.getTargetAmount());
        savingGoalResponseDTO.setCompletionDate(savingGoal.getCompletionDate());
        savingGoalResponseDTO.setTargetDate(savingGoal.getTargetDate());

        return savingGoalResponseDTO;
    }
}
