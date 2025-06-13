package finance.freedom.finance_freedom_backend.interfaces.core;

import finance.freedom.finance_freedom_backend.dto.user.RefreshTokenRequest;
import finance.freedom.finance_freedom_backend.dto.user.UserDetailsRequestDTO;
import finance.freedom.finance_freedom_backend.model.core.User;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.model.security.JWT;
import jakarta.mail.MessagingException;

public interface IUserService {

    UserDetailsRequestDTO getUserDetails(User user);

    GenericResponse deleteAccount (User user) throws MessagingException;

    JWT refresh (User user, RefreshTokenRequest refreshTokenRequest);

    GenericResponse logout (User user, RefreshTokenRequest refreshTokenRequest);

    GenericResponse resetPassword(User user) throws MessagingException;
}
