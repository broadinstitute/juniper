package bio.terra.pearl.core.service.file;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Getter
@Setter
@Configuration
public class FileStorageConfig {
    private String defaultBackend;
    private String localFileStoragePath;
    private String gcsStorageBucketName;
    private Storage gcsStorageConfig;

    public FileStorageConfig(Environment environment) {
        this.defaultBackend = environment.getProperty("env.fileUpload.backend", "LocalFileStorageBackend");
        this.localFileStoragePath = environment.getProperty("env.fileUpload.localFileStoragePath");
        this.gcsStorageBucketName = environment.getProperty("env.fileUpload.gcsFileStorageBucketName");
        this.gcsStorageConfig = StorageOptions.newBuilder().setProjectId(environment.getProperty("env.fileUpload.gcsFileStorageProjectId")).build().getService();
    }
}
