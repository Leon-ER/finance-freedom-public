package finance.freedom.finance_freedom_backend.model.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class GenericResponse {
    private String message;
    private LocalDateTime timestamp;

    public GenericResponse(String message) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
