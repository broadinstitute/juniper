package bio.terra.pearl.core.service.file.backends;

import bio.terra.pearl.core.service.file.FileStorageConfig;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class FileStorageBackendProvider {

    private final Map<String, FileStorageBackend> backendMap = new HashMap<>();
    private final String defaultBackend;

    public FileStorageBackendProvider(
            FileStorageConfig fileStorageConfig,
            LocalFileStorageBackend localFileStorageBackend,
            GCSFileStorageBackend gcsFileStorageBackend) {
        defaultBackend = fileStorageConfig.getDefaultBackend();
        backendMap.put("LocalFileStorageBackend", localFileStorageBackend);
        backendMap.put("GCSFileStorageBackend", gcsFileStorageBackend);
    }

    public FileStorageBackend get() {
        return backendMap.get(defaultBackend);
    }

    public FileStorageBackend get(String client) {
        return backendMap.get(client);
    }

}
