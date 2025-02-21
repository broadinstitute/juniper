package bio.terra.pearl.core.service.file.backends;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

@Service
public class GCSFileStorageBackend implements FileStorageBackend {

    private final String BUCKET_NAME = "mb-test-juniper-upload-bucket";
    private final String PROJECT_NAME = "broad-juniper-dev";

    @Override
    public UUID uploadFile(InputStream data) {
        UUID fileId = UUID.randomUUID();

        Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_NAME).build().getService();
        BlobId blobId = BlobId.of(BUCKET_NAME, fileId.toString());
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
        Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_NAME).build().getService();
        BlobId blobId = BlobId.of(BUCKET_NAME, uploadedFileId.toString());

        try {
            byte[] gcsBytes = storage.get(blobId).getContent();
            return new ByteArrayInputStream(gcsBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to download file from GCS", e);
        }
    }

    @Override
    public void deleteFile(UUID uploadedFileId) {
        Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_NAME).build().getService();
        BlobId blobId = BlobId.of(BUCKET_NAME, uploadedFileId.toString());

        try {
            storage.delete(blobId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from GCS", e);
        }
    }
}
