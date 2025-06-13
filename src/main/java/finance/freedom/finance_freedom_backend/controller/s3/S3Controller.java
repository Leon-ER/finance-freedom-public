package finance.freedom.finance_freedom_backend.controller.s3;

import finance.freedom.finance_freedom_backend.interfaces.aws.IAwsS3Service;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import finance.freedom.finance_freedom_backend.model.security.CustomUserDetails;
import finance.freedom.finance_freedom_backend.util.AuthorizationUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/s3")
@RequiredArgsConstructor
public class S3Controller {
    private final IAwsS3Service s3;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GenericResponse> uploadReport(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                                        @RequestParam("file") MultipartFile file) throws IOException {

        AuthorizationUtils.requireUser(customUserDetails);

        return ResponseEntity.status(HttpStatus.CREATED).body(s3.putObject(customUserDetails.getUser().getUserId(), file.getBytes()));
    }

    @GetMapping("{date}")
    public ResponseEntity<String> getFile(@AuthenticationPrincipal CustomUserDetails customUserDetails,
                                          @PathVariable String date) throws IOException {
        AuthorizationUtils.requireUser(customUserDetails);

        LocalDateTime parsedDate = LocalDateTime.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        return ResponseEntity.ok(new String(s3.getObject(customUserDetails.getUser().getUserId(), parsedDate.toString())));
    }

    @GetMapping()
    public ResponseEntity<List<String>> getAllFiles(@AuthenticationPrincipal CustomUserDetails customUserDetails){
        AuthorizationUtils.requireUser(customUserDetails);

        return ResponseEntity.ok(s3.listUserReports(customUserDetails.getUser().getUserId()));
    }
}
