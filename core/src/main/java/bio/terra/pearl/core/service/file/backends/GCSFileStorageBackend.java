package bio.terra.pearl.core.service.file.backends;

import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.file.FileStorageConfig;
import bio.terra.pearl.core.service.file.VirusScanResult;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@Service
public class GCSFileStorageBackend implements FileStorageBackend {

    private final String unscannedBucketName;
    private final String cleanBucketName;
    private final String quarantinedBucketName;
    private final Storage storage;

    public GCSFileStorageBackend(FileStorageConfig storageConfig) {
        this.storage = storageConfig.getGcsStorageConfig();
        this.unscannedBucketName = storageConfig.getGcsStorageBucketName();
    }

    @Override
    public UUID uploadFile(InputStream data) {
        UUID fileId = UUID.randomUUID();

        BlobId blobId = BlobId.of(unscannedBucketName, fileId.toString());
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

        try {
            storage.createFrom(blobInfo, data);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to GCS", e);
        }

        return fileId;
    }

    @Override
    public InputStream downloadFile(UUID uploadedFileId) {
        BlobId cleanBlobId = BlobId.of(cleanBucketName, uploadedFileId.toString());
        try {
            Blob blob = storage.get(cleanBlobId);

            if (blob == null) {
                blob = storage.get(BlobId.of(unscannedBucketName, uploadedFileId.toString()));
            }

            if (blob == null) {
                throw new NotFoundException("File not found in GCS");
            }

            byte[] gcsBytes = blob.getContent();
            return new ByteArrayInputStream(gcsBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from GCS", e);
        }
    }

    @Override
    public VirusScanResult scanResult(UUID uploadedFileId) {
        BlobId cleanBlobId = BlobId.of(cleanBucketName, uploadedFileId.toString());
        BlobId unscannedBlobId = BlobId.of(unscannedBucketName, uploadedFileId.toString());
        BlobId quarantinedBlobId = BlobId.of(quarantinedBucketName, uploadedFileId.toString());

        if (storage.get(cleanBlobId) != null) {
            return VirusScanResult.CLEAN;
        } else if (storage.get(unscannedBlobId) != null) {
            return VirusScanResult.UNSCANNED;
        } else if (storage.get(quarantinedBlobId) != null) {
            return VirusScanResult.QUARANTINED;
        }

        throw new NotFoundException("File not found in GCS");
    }

    @Override
    public void deleteFile(UUID uploadedFileId) {
        BlobId blobId = BlobId.of(cleanBucketName, uploadedFileId.toString());

        if (storage.get(blobId) == null) {
            blobId = BlobId.of(unscannedBucketName, uploadedFileId.toString());
        }

        try {
            storage.delete(blobId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from GCS", e);
        }
    }
}
