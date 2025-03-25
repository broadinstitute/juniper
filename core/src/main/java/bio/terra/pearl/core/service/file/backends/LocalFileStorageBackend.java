package bio.terra.pearl.core.service.file.backends;

import bio.terra.pearl.core.service.file.FileStorageConfig;
import bio.terra.pearl.core.service.file.VirusScanResult;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.util.UUID;

@Service
public class LocalFileStorageBackend implements FileStorageBackend {
    private final String localFileStoragePath;

    public LocalFileStorageBackend(FileStorageConfig fileStorageConfig) {
        this.localFileStoragePath = fileStorageConfig.getLocalFileStoragePath();
    }

    @Override
    public UUID uploadFile(InputStream data) {
        UUID fileId = UUID.randomUUID();

        File file = getFile(fileId);

        try {
            FileUtils.copyInputStreamToFile(data, file);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to local storage", e);
        }

        return fileId;
    }

    @Override
    public InputStream downloadFile(UUID uploadedFileId) {
        File file = getFile(uploadedFileId);

        if (file.exists()) {
            try {
                return FileUtils.openInputStream(file);
            } catch (Exception e) {
                throw new RuntimeException("Failed to download file from local storage", e);
            }
        }
        return null;
    }

    @Override
    public VirusScanResult scanResult(UUID uploadedFileId) {
        return VirusScanResult.CLEAN;
    }

    @Override
    public void deleteFile(UUID uploadedFileId) {
        File file = getFile(uploadedFileId);

        if (file.exists()) {
            file.delete();
        }

    }

    private File getFile(UUID uploadedFileId) {
        return new File(localFileStoragePath + "/" + uploadedFileId);
    }
}
