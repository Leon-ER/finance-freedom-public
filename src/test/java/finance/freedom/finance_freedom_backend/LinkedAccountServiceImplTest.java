package finance.freedom.finance_freedom_backend;

import finance.freedom.finance_freedom_backend.dto.linkedaccount.CreateLinkedAccountDTO;
import finance.freedom.finance_freedom_backend.dto.linkedaccount.LinkedAccountResponseDTO;
import finance.freedom.finance_freedom_backend.dto.linkedaccount.UpdateLinkedAccountDTO;
import finance.freedom.finance_freedom_backend.exception.customexceptions.LinkedAccountNotFoundException;
import finance.freedom.finance_freedom_backend.model.core.LinkedAccount;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.enums.AccountType;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.repository.LinkedAccountRepository;
import finance.freedom.finance_freedom_backend.service.core.LinkedAccountServiceImpl;
import finance.freedom.finance_freedom_backend.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LinkedAccountServiceImplTest {

    @Mock
    private LinkedAccountRepository linkedAccountRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @InjectMocks
    private LinkedAccountServiceImpl linkedAccountService;

    private User user;
    private Integer accountId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setUserId(1);
        accountId = 101;
    }

    @Test
    void createLinkedAccount_validInput_returnsDTO() {
        CreateLinkedAccountDTO dto = new CreateLinkedAccountDTO();
        dto.setAccountName("My Account");
        dto.setAccountType(AccountType.CHECKING);
        dto.setAccessToken("access-token");
        dto.setInstitutionName("My Bank");

        when(encryptionUtil.encrypt(dto.getAccessToken())).thenReturn("encrypted-token");

        LinkedAccountResponseDTO result = linkedAccountService.createLinkedAccount(user, dto);

        assertEquals(dto.getAccountName(), result.getAccountName());
        assertEquals(dto.getAccountType(), result.getAccountType());
    }

    @Test
    void getLinkedAccountByUser_returnsMapOfDTOs() {
        LinkedAccount linkedAccount = new LinkedAccount();
        linkedAccount.setAccountId(accountId);
        linkedAccount.setUser(user);
        linkedAccount.setAccountName("Test");
        linkedAccount.setAccountType(AccountType.SAVINGS);
        linkedAccount.setBalance(BigDecimal.TEN);

        when(linkedAccountRepository.findByUser(user)).thenReturn(List.of(linkedAccount));

        Map<Integer, LinkedAccountResponseDTO> result = linkedAccountService.getLinkedAccountByUser(user);

        assertTrue(result.containsKey(accountId));
        assertEquals("Test", result.get(accountId).getAccountName());
    }

    @Test
    void updateLinkedAccount_validInput_updatesAndReturnsDTO() {
        LinkedAccount linkedAccount = new LinkedAccount();
        linkedAccount.setAccountId(accountId);
        linkedAccount.setUser(user);

        UpdateLinkedAccountDTO updateDTO = new UpdateLinkedAccountDTO();
        updateDTO.setAccountName("Updated Name");
        updateDTO.setAccountType(AccountType.CREDIT);
        updateDTO.setAccessToken("new-token");
        updateDTO.setInstitutionName("New Inst");
        updateDTO.setBalance(BigDecimal.valueOf(200));

        when(linkedAccountRepository.findByAccountId(accountId)).thenReturn(linkedAccount);

        LinkedAccountResponseDTO result = linkedAccountService.updateLinkedAccount(user, accountId, updateDTO);

        assertEquals(updateDTO.getAccountName(), result.getAccountName());
        verify(linkedAccountRepository).save(linkedAccount);
    }

    @Test
    void updateLinkedAccount_notFound_throwsException() {
        when(linkedAccountRepository.findByAccountId(accountId)).thenReturn(null);

        assertThrows(LinkedAccountNotFoundException.class, () ->
                linkedAccountService.updateLinkedAccount(user, accountId, new UpdateLinkedAccountDTO())
        );
    }

    @Test
    void getLinkedAccountById_validInput_returnsDTO() {
        LinkedAccount linkedAccount = new LinkedAccount();
        linkedAccount.setAccountId(accountId);
        linkedAccount.setUser(user);

        when(linkedAccountRepository.findByAccountId(accountId)).thenReturn(linkedAccount);

        LinkedAccountResponseDTO result = linkedAccountService.getLinkedAccountById(user, accountId);

        assertEquals(accountId, result.getAccountId());
    }

    @Test
    void deleteLinkedAccount_validInput_deletesSuccessfully() {
        LinkedAccount linkedAccount = new LinkedAccount();
        linkedAccount.setAccountId(accountId);
        linkedAccount.setUser(user);

        when(linkedAccountRepository.findByAccountId(accountId)).thenReturn(linkedAccount);

        GenericResponse response = linkedAccountService.deleteLinkedAccount(user, accountId);

        assertTrue(response.getMessage().contains("deleted successfully"));
        verify(linkedAccountRepository).delete(linkedAccount);
    }

    @Test
    void deleteLinkedAccount_notFound_throwsException() {
        when(linkedAccountRepository.findByAccountId(accountId)).thenReturn(null);

        assertThrows(LinkedAccountNotFoundException.class, () ->
                linkedAccountService.deleteLinkedAccount(user, accountId)
        );
    }
}
