package finance.freedom.finance_freedom_backend.service.core;

import finance.freedom.finance_freedom_backend.dto.linkedaccount.CreateLinkedAccountDTO;
import finance.freedom.finance_freedom_backend.dto.linkedaccount.LinkedAccountResponseDTO;
import finance.freedom.finance_freedom_backend.dto.linkedaccount.UpdateLinkedAccountDTO;
import finance.freedom.finance_freedom_backend.exception.customexceptions.LinkedAccountNotFoundException;
import finance.freedom.finance_freedom_backend.interfaces.core.ILinkedAccountService;
import finance.freedom.finance_freedom_backend.model.core.LinkedAccount;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.repository.LinkedAccountRepository;
import finance.freedom.finance_freedom_backend.util.AuthorizationUtils;
import finance.freedom.finance_freedom_backend.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class LinkedAccountServiceImpl implements ILinkedAccountService {

    private final LinkedAccountRepository linkedAccountRepository;

    private final EncryptionUtil encryptionUtil;


    @Override
    public LinkedAccountResponseDTO createLinkedAccount(User user, CreateLinkedAccountDTO createLinkedAccountDTO) {
        log.info("Attempting to save linked account for user {}", user.getEmail());
        LinkedAccount linkedAccount = new LinkedAccount();

        linkedAccount.setUser(user);
        linkedAccount.setAccountName(createLinkedAccountDTO.getAccountName());
        linkedAccount.setAccountType(createLinkedAccountDTO.getAccountType());
        linkedAccount.setAccessToken(encryptionUtil.encrypt(createLinkedAccountDTO.getAccessToken()));
        linkedAccount.setInstitutionName(createLinkedAccountDTO.getInstitutionName());
        linkedAccount.setLastUpdate(LocalDateTime.now());
        linkedAccount.setBalance(createLinkedAccountDTO.getBalance());


        linkedAccountRepository.save(linkedAccount);
        log.info("Linked account saved successfully {}", linkedAccount.getAccountId());
        return createDTO(linkedAccount);
    }

    @Override
    public Map<Integer, LinkedAccountResponseDTO> getLinkedAccountByUser(User user) {
        log.info("Attempting to get linked accounts for user {}", user.getEmail());
        List<LinkedAccount> linkedAccounts = linkedAccountRepository.findByUser(user);

        if(linkedAccounts.isEmpty()) {
            log.warn("Linked accounts for user not found");
            throw new LinkedAccountNotFoundException("Linked accounts for user not found");
        }

        Map<Integer, LinkedAccountResponseDTO> result = new HashMap();

        for(LinkedAccount linkedAccount : linkedAccounts) {
            result.put(linkedAccount.getAccountId(), createDTO(linkedAccount));
        }
        log.info("Linked accounts found successfully");
        return result;
    }

    @Override
    public LinkedAccountResponseDTO updateLinkedAccount(User user, Integer linkedAccountId, UpdateLinkedAccountDTO linkedAccountUpdateDTO) {
        log.info("Attempting to update linked account by id {}", linkedAccountId);

        LinkedAccount linkedAccount = linkedAccountRepository.findByAccountId(linkedAccountId);

        if (linkedAccount == null) {
            log.warn("Linked account not found for id: {}", linkedAccountId);
            throw new LinkedAccountNotFoundException("Linked account not found");
        }
        AuthorizationUtils.validateOwnership(linkedAccount.getUser().getUserId(), user.getUserId());

        if(linkedAccountUpdateDTO.getAccountName() != null) {
            linkedAccount.setAccountName(linkedAccountUpdateDTO.getAccountName());
        }
        if(linkedAccountUpdateDTO.getAccountType() != null) {
            linkedAccount.setAccountType(linkedAccountUpdateDTO.getAccountType());
        }
        if(linkedAccountUpdateDTO.getAccessToken() != null) {
            linkedAccount.setAccessToken(linkedAccountUpdateDTO.getAccessToken());
        }
        if(linkedAccountUpdateDTO.getInstitutionName() != null) {
            linkedAccount.setInstitutionName(linkedAccountUpdateDTO.getInstitutionName());
        }
        if(linkedAccountUpdateDTO.getBalance() != null) {
            linkedAccount.setBalance(linkedAccountUpdateDTO.getBalance());
        }
        linkedAccount.setLastUpdate(LocalDateTime.now());

        linkedAccountRepository.save(linkedAccount);

        log.info("Linked account updated successfully {}", linkedAccount.getAccountId());

        return createDTO(linkedAccount);
    }

    @Override
    public LinkedAccountResponseDTO getLinkedAccountById(User user, Integer linkedAccountId) {
        log.info("Attempting to get linked account by id {}", linkedAccountId);
        LinkedAccount linkedAccount = linkedAccountRepository.findByAccountId(linkedAccountId);

        if (linkedAccount == null) {
            log.warn("Linked account not found for id: {}", linkedAccountId);
            throw new LinkedAccountNotFoundException("Linked account not found");
        }
        AuthorizationUtils.validateOwnership(linkedAccount.getUser().getUserId(), user.getUserId());

        log.info("Linked account found successfully {}", linkedAccount.getAccountId());
        return createDTO(linkedAccount);
    }

    @Override
    public LinkedAccountResponseDTO refreshAccount(User user, Integer linkedAccountId) {

        //ToDo when front end is up.
        return null;
    }

    @Override
    public GenericResponse deleteLinkedAccount(User user, Integer linkedAccountId) {
        log.info("Attempting to delete linked account by id {}", linkedAccountId);

        LinkedAccount linkedAccount = linkedAccountRepository.findByAccountId(linkedAccountId);
        if (linkedAccount == null) {
            log.warn("Linked account not found for id: {}", linkedAccountId);
            throw new LinkedAccountNotFoundException("Linked account not found");
        }
        AuthorizationUtils.validateOwnership(linkedAccount.getUser().getUserId(), user.getUserId());

        linkedAccountRepository.delete(linkedAccount);
        log.info("Linked account deleted successfully {}", linkedAccountId);
        return new GenericResponse(String.format("Linked account with id: %s deleted successfully",linkedAccountId));
    }

    LinkedAccountResponseDTO createDTO(LinkedAccount linkedAccount) {
        LinkedAccountResponseDTO linkedAccountResponseDTO = new LinkedAccountResponseDTO();

        linkedAccountResponseDTO.setAccountId(linkedAccount.getAccountId());
        linkedAccountResponseDTO.setAccountType(linkedAccount.getAccountType());
        linkedAccountResponseDTO.setAccountName(linkedAccount.getAccountName());
        linkedAccountResponseDTO.setBalance(linkedAccount.getBalance());

        return linkedAccountResponseDTO;
    }
}
