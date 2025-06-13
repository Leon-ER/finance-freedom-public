package finance.freedom.finance_freedom_backend.service.aws;

import finance.freedom.finance_freedom_backend.interfaces.aws.IAwsS3Service;
import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AwsS3ServiceImpl implements IAwsS3Service {

    private final S3Client s3;

    @Value("${aws.bucketName}")
    private String bucketName;

    @Override
    public void createFolder(Integer userId) {
        log.info("Attempting to create S3 folder for user {}", userId);
        String key = String.format("reports/user-%s/.init", userId);
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3.putObject(objectRequest, RequestBody.empty());
        log.info("Folder created successfully for user {}", userId);

    }

    @Override
    public GenericResponse putObject(Integer userId, byte[] file) {
        log.info("Attempting to upload to S3 folder for user {}", userId);
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String key = String.format("reports/user-%s/report-%s", userId, timestamp);
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        s3.putObject(objectRequest, RequestBody.fromBytes(file));

        return new GenericResponse(String.format("File created successfully for user %s", userId));
    }

    @Override
    public byte[] getObject(Integer userID, String fileName) throws IOException {
        log.info("Attempting to fetch reports for user {}", userID);
        String key = String.format("reports/user-%s/report-%s", userID, fileName);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        ResponseInputStream<GetObjectResponse> response = s3.getObject(getObjectRequest);

        byte[] reports = response.readAllBytes();

        log.info("Reports fetched successfully for user {}", userID);

        return reports;
    }

    @Override
    public List<String> listUserReports(Integer userId) {
        String prefix = String.format("reports/user-%s/", userId);

        ListObjectsV2Request listRequest = ListObjectsV2Request.builder()
                .bucket(bucketName)
                .prefix(prefix)
                .build();

        ListObjectsV2Response listResponse = s3.listObjectsV2(listRequest);

        return listResponse.contents().stream()
                .map(S3Object::key)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteFolder(Integer userId) {
        log.info("Attempting to delete folder for user {}", userId);

        List<String> objectKeys = listUserReports(userId);

        if (objectKeys.isEmpty()) {
            log.warn("No objects found under folder for user {}", userId);
            return false;
        }

        for (String key : objectKeys) {
            s3.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            log.debug("Deleted object: {}", key);
        }

        log.info("Folder and all contents deleted for user {}", userId);
        return true;
    }

}
