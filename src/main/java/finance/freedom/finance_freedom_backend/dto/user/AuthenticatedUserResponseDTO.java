package finance.freedom.finance_freedom_backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatedUserResponseDTO {
    private Integer userId;
    private String username;
    private String email;
    private String accessToken;
    private Date expiresAt;
}
