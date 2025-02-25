package bio.terra.pearl.core.service.file.backends;

import bio.terra.pearl.core.service.file.FileStorageConfig;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
        when(fileStorageConfig.getGcsStorageBucketName()).thenReturn("test-bucket");

        gcsFileStorageBackend = new GCSFileStorageBackend(fileStorageConfig);
    }

    @Test
    void testUploadFile() throws IOException {
        UUID fileId = UUID.fromString("906c11cd-5e92-487b-a386-ac300e980861");
        InputStream data = new ByteArrayInputStream("test data".getBytes());

        MockedStatic<UUID> mocked = mockStatic(UUID.class);
        mocked.when(UUID::randomUUID).thenReturn(fileId);
        when(storage.createFrom(any(BlobInfo.class), any(InputStream.class))).thenReturn(null);

        UUID result = gcsFileStorageBackend.uploadFile(data);

        assertEquals(fileId,result);
        verify(storage, times(1)).createFrom(any(BlobInfo.class), any(InputStream.class));
    }

    @Test
    void testDownloadFile() throws IOException {
        UUID fileId = UUID.fromString("906c11cd-5e92-487b-a386-ac300e980861");
        byte[] fileContent = "test data".getBytes();

        Blob blob = mock(Blob.class);
        when(blob.getContent()).thenReturn(fileContent);
        when(storage.get(any(BlobId.class))).thenReturn(blob);

        InputStream result = gcsFileStorageBackend.downloadFile(fileId);

        assertNotNull(result);
        byte[] resultBytes = result.readAllBytes();
        assertArrayEquals(fileContent, resultBytes);

        verify(storage, times(1)).get(any(BlobId.class));
    }

    @Test
    void testDeleteFile() {
        UUID fileId = UUID.fromString("906c11cd-5e92-487b-a386-ac300e980861");

        when(storage.delete(any(BlobId.class))).thenReturn(true);

        gcsFileStorageBackend.deleteFile(fileId);

        verify(storage, times(1)).delete(any(BlobId.class));
    }

}
