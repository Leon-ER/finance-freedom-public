package finance.freedom.finance_freedom_backend.interfaces.aws;

import finance.freedom.finance_freedom_backend.model.exception.GenericResponse;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public interface IAwsS3Service {

    void createFolder(Integer userId);

    GenericResponse putObject(Integer userId, byte[] file);

    byte[] getObject(Integer userID, String fileName) throws IOException;

    List<String> listUserReports(Integer userId);

    boolean deleteFolder(Integer userId);
}
