package finance.freedom.finance_freedom_backend.interfaces.core;

import finance.freedom.finance_freedom_backend.dto.linkedaccount.CreateLinkedAccountDTO;
import finance.freedom.finance_freedom_backend.dto.linkedaccount.LinkedAccountResponseDTO;
import finance.freedom.finance_freedom_backend.dto.linkedaccount.UpdateLinkedAccountDTO;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;

import java.util.Map;
import java.util.UUID;

public interface ILinkedAccountService {
    LinkedAccountResponseDTO createLinkedAccount(User user, CreateLinkedAccountDTO createLinkedAccountDTO);
    Map<Integer,LinkedAccountResponseDTO> getLinkedAccountByUser(User user);
    LinkedAccountResponseDTO updateLinkedAccount(User user, Integer linkedAccountId, UpdateLinkedAccountDTO linkedAccountResponseDTO);
    LinkedAccountResponseDTO getLinkedAccountById(User user, Integer linkedAccountId);
    LinkedAccountResponseDTO refreshAccount(User user, Integer linkedAccountId);
    GenericResponse deleteLinkedAccount(User user, Integer linkedAccountId);
}
