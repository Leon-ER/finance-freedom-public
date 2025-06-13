package finance.freedom.finance_freedom_backend.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import finance.freedom.finance_freedom_backend.exception.customexceptions.*;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import finance.freedom.finance_freedom_backend.model.exception.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.S3Exception;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalControllerExceptionHandler{

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<GenericResponse> handleRunTimeException(RuntimeException ex){
        log.error("RuntimeException catch: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericResponse(String.format("Something went wrong: %s", ex.getMessage())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        log.error("Validation exception catch{}", String.valueOf(ex));
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String field = error.getField();
            String message = error.getDefaultMessage();
            errors.put(field, message);
        });

        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_ERROR",
                "One or more fields are invalid",
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GenericResponse> handleIllegalArgumentException(RuntimeException ex){
        log.error("IllegalArgumentException catch{}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new GenericResponse(String.format("Something went wrong: %s", ex.getMessage())));
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<GenericResponse> handleTooManyRequestException(Exception ex){
        log.error("TooManyRequestsException catch{}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(new GenericResponse(String.format(ex.getMessage())));
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<GenericResponse> handleExpiredJWTException(ExpiredJwtException ex){
        log.error("ExpiredJwtException catch{}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new GenericResponse(String.format(ex.getMessage())));
    }

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<GenericResponse> handleTransactionNotFoundException(Exception ex){
        log.error("TransactionNotFoundException catch{}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GenericResponse(String.format(ex.getMessage())));
    }
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<GenericResponse> handleUserNotFoundException(Exception ex){
        log.error("UserNotFoundException catch{}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GenericResponse(String.format(ex.getMessage())));
    }
    @ExceptionHandler(SavingGoalNotFoundException.class)
    public ResponseEntity<GenericResponse> handleSavingGoalNotFoundException(Exception ex) {
        log.error("SavingGoalNotFoundException catch{}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GenericResponse(String.format(ex.getMessage())));
    }
    @ExceptionHandler(MessagingException.class)
    public ResponseEntity<GenericResponse> handleMessagingException(MessagingException ex){
        log.error("MessagingException catch{}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericResponse(String.format(ex.getMessage())));

    }
    @ExceptionHandler(BudgetNotFoundException.class)
    public ResponseEntity<GenericResponse> handleBudgetNotFoundException(Exception ex){
        log.error("BudgetNotFoundException catch{}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GenericResponse(String.format(ex.getMessage())));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GenericResponse> handleAccessDeniedException(Exception ex){
        log.error("AccessDeniedException catch{}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new GenericResponse(String.format(ex.getMessage())));
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<GenericResponse> handleJsonProcessingException(JsonProcessingException ex){
        log.error("JsonProcessingException catch{}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenericResponse(String.format(ex.getMessage())));
    }

    @ExceptionHandler(LinkedAccountNotFoundException.class)
    public ResponseEntity<GenericResponse> handleLinkedAccountNotFoundException(Exception ex){
        log.error("LinkedTransactionNotFoundException catch{}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new GenericResponse(String.format(ex.getMessage())));
    }

    @ExceptionHandler(S3Exception.class)
    public ResponseEntity<GenericResponse> handleS3Exception(S3Exception ex) {
        log.error("AWS S3 error: {}", ex.awsErrorDetails().errorMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(new GenericResponse("An error occurred while communicating with AWS S3: " + ex.awsErrorDetails().errorMessage()));
    }

    @ExceptionHandler(SdkClientException.class)
    public ResponseEntity<GenericResponse> handleSdkClientException(SdkClientException ex) {
        log.error("AWS SDK client error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new GenericResponse("Unable to connect to AWS services. Please try again later."));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<GenericResponse> handleIOException(IOException ex) {
        log.error("IO error while processing S3 object: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new GenericResponse("An error occurred while processing the file."));
    }

}
