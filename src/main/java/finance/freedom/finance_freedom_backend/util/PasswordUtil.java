package finance.freedom.finance_freedom_backend.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordUtil {
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public String hash(String password){
        return bCryptPasswordEncoder.encode(password);
    }

    public boolean verify(String plainPassword, String hashedPassword){
        return bCryptPasswordEncoder.matches(plainPassword, hashedPassword);
    }
}
