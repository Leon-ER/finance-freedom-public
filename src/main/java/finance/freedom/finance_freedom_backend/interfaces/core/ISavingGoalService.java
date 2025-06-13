package finance.freedom.finance_freedom_backend.interfaces.core;

import finance.freedom.finance_freedom_backend.dto.savinggoal.CreateSavingGoalDTO;
import finance.freedom.finance_freedom_backend.dto.savinggoal.SavingGoalResponseDTO;
import finance.freedom.finance_freedom_backend.dto.savinggoal.UpdateSavingGoalDTO;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;

import java.util.Map;
import java.util.UUID;

public interface ISavingGoalService {

    SavingGoalResponseDTO save(User user, CreateSavingGoalDTO savingGoal);

    SavingGoalResponseDTO getSavingGoalById(User user, Integer savingGoalId);

    Map<Integer,SavingGoalResponseDTO> getSavingGoal(User user);

    SavingGoalResponseDTO updateSavingGoal(User user,Integer savingGoalId, UpdateSavingGoalDTO savingGoal);

    GenericResponse deleteSavingGoal(User user, Integer savingGoalId);

}
