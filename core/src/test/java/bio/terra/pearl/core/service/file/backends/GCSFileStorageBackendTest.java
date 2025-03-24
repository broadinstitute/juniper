package bio.terra.pearl.core.service.file.backends;

import bio.terra.pearl.core.service.file.FileStorageConfig;
import bio.terra.pearl.core.service.file.VirusScanResult;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class GCSFileStorageBackendTest {

    @Mock
    private Storage storage;

    @Mock
    private FileStorageConfig fileStorageConfig;

    @InjectMocks
    private GCSFileStorageBackend gcsFileStorageBackend;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        when(fileStorageConfig.getGcsStorageConfig()).thenReturn(storage);
        when(fileStorageConfig.getGcsStorageCleanBucketName()).thenReturn("test-bucket-clean");
        when(fileStorageConfig.getGcsStorageUnscannedBucketName()).thenReturn("test-bucket-unscanned");
        when(fileStorageConfig.getGcsStorageQuarantinedBucketName()).thenReturn("test-bucket-quarantined");

        gcsFileStorageBackend = new GCSFileStorageBackend(fileStorageConfig);
    }

    @Test
    void testUploadFile() throws IOException {
        InputStream data = new ByteArrayInputStream("test data".getBytes());

        when(storage.createFrom(any(BlobInfo.class), any(InputStream.class))).thenReturn(null);

        UUID result = gcsFileStorageBackend.uploadFile(data);

        Assertions.assertNotNull(result);
        verify(storage, times(1)).createFrom(any(BlobInfo.class), any(InputStream.class));
    }

    @Test
    void testDownloadFile() throws IOException {
        UUID fileId = UUID.fromString("906c11cd-5e92-487b-a386-ac300e980861");
        byte[] fileContent = "test data".getBytes();

        Blob blob = mock(Blob.class);
        when(blob.getContent()).thenReturn(fileContent);
        when(storage.get(BlobId.of("test-bucket-clean", fileId.toString()))).thenReturn(blob);
        when(storage.get(BlobId.of("test-bucket-unscanned", fileId.toString()))).thenReturn(null);

        InputStream result = gcsFileStorageBackend.downloadFile(fileId);

        Assertions.assertNotNull(result);
        byte[] resultBytes = result.readAllBytes();
        Assertions.assertArrayEquals(fileContent, resultBytes);

        // only attempt clean bucket; since it's found, don't attempt unscanned
        BlobId cleanBlobId = BlobId.of("test-bucket-clean", fileId.toString());
        BlobId unscannedBlobId = BlobId.of("test-bucket-unscanned", fileId.toString());
        verify(storage, times(1)).get(cleanBlobId);
        verify(storage, times(0)).get(unscannedBlobId);
    }

    @Test
    void testDownloadUnscannedFile() throws IOException {
        UUID fileId = UUID.fromString("906c11cd-5e92-487b-a386-ac300e980861");
        byte[] fileContent = "test data".getBytes();

        Blob blob = mock(Blob.class);
        when(blob.getContent()).thenReturn(fileContent);
        when(storage.get(BlobId.of("test-bucket-clean", fileId.toString()))).thenReturn(null);
        when(storage.get(BlobId.of("test-bucket-unscanned", fileId.toString()))).thenReturn(blob);

        InputStream result = gcsFileStorageBackend.downloadFile(fileId);

        Assertions.assertNotNull(result);
        byte[] resultBytes = result.readAllBytes();
        Assertions.assertArrayEquals(fileContent, resultBytes);

        // attempts to download from both clean and unscanned buckets
        BlobId cleanBlobId = BlobId.of("test-bucket-clean", fileId.toString());
        BlobId unscannedBlobId = BlobId.of("test-bucket-unscanned", fileId.toString());
        verify(storage, times(1)).get(cleanBlobId);
        verify(storage, times(1)).get(unscannedBlobId);
    }

    @Test
    void testDeleteFile() {
        UUID fileId = UUID.fromString("906c11cd-5e92-487b-a386-ac300e980861");

        when(storage.get(BlobId.of("test-bucket-clean", fileId.toString()))).thenReturn(mock(Blob.class));
        when(storage.delete(any(BlobId.class))).thenReturn(true);

        gcsFileStorageBackend.deleteFile(fileId);

        BlobId expectedBlobId = BlobId.of("test-bucket-clean", fileId.toString());
        verify(storage, times(1)).delete(expectedBlobId);
    }

    @Test
    void testDeleteFileAlsoSearchesUnscanned() {
        UUID fileId = UUID.fromString("906c11cd-5e92-487b-a386-ac300e980861");

        when(storage.get(BlobId.of("test-bucket-clean", fileId.toString()))).thenReturn(null);
        when(storage.get(BlobId.of("test-bucket-unscanned", fileId.toString()))).thenReturn(mock(Blob.class));

        when(storage.delete(any(BlobId.class))).thenReturn(true);

        gcsFileStorageBackend.deleteFile(fileId);

        BlobId expectedBlobId = BlobId.of("test-bucket-unscanned", fileId.toString());
        verify(storage, times(1)).delete(expectedBlobId);
    }

    @Test
    void testVirusScanClean() {
        UUID fileId = UUID.fromString("906c11cd-5e92-487b-a386-ac300e980861");

        BlobId cleanBlobId = BlobId.of("test-bucket-clean", fileId.toString());
        BlobId unscannedBlobId = BlobId.of("test-bucket-unscanned", fileId.toString());
        BlobId quarantinedBlobId = BlobId.of("test-bucket-quarantined", fileId.toString());
        when(storage.get(cleanBlobId)).thenReturn(mock(Blob.class));
        when(storage.get(unscannedBlobId)).thenReturn(null);
        when(storage.get(quarantinedBlobId)).thenReturn(null);

        Assertions.assertEquals(gcsFileStorageBackend.scanResult(fileId), VirusScanResult.CLEAN);
    }

    @Test
    void testVirusScanUnscanned() {
        UUID fileId = UUID.fromString("906c11cd-5e92-487b-a386-ac300e980861");

        BlobId cleanBlobId = BlobId.of("test-bucket-clean", fileId.toString());
        BlobId unscannedBlobId = BlobId.of("test-bucket-unscanned", fileId.toString());
        BlobId quarantinedBlobId = BlobId.of("test-bucket-quarantined", fileId.toString());
        when(storage.get(cleanBlobId)).thenReturn(null);
        when(storage.get(unscannedBlobId)).thenReturn(mock(Blob.class));
        when(storage.get(quarantinedBlobId)).thenReturn(null);

        Assertions.assertEquals(gcsFileStorageBackend.scanResult(fileId), VirusScanResult.UNSCANNED);
    }

    @Test
    void testVirusScanQuarantined() {
        UUID fileId = UUID.fromString("906c11cd-5e92-487b-a386-ac300e980861");

        BlobId cleanBlobId = BlobId.of("test-bucket-clean", fileId.toString());
        BlobId unscannedBlobId = BlobId.of("test-bucket-unscanned", fileId.toString());
        BlobId quarantinedBlobId = BlobId.of("test-bucket-quarantined", fileId.toString());
        when(storage.get(cleanBlobId)).thenReturn(null);
        when(storage.get(unscannedBlobId)).thenReturn(null);
        when(storage.get(quarantinedBlobId)).thenReturn(mock(Blob.class));

        Assertions.assertEquals(gcsFileStorageBackend.scanResult(fileId), VirusScanResult.QUARANTINED);
    }

}
