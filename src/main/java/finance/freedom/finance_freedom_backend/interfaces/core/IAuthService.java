package finance.freedom.finance_freedom_backend.interfaces.core;

import finance.freedom.finance_freedom_backend.dto.user.*;

import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import jakarta.mail.MessagingException;


public interface IAuthService {
    CreateUserResponseDTO save(CreateUserDTO user)throws MessagingException ;

    AuthenticatedUserResponseDTO login(LoginRequestDTO loginRequestDTO);

    GenericResponse passwordReset(String email) throws MessagingException;
}
