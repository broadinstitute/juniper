package bio.terra.pearl.core.service.azure;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

@Component
public class AzureBlobStorageClient {

    private Environment env;

    @Autowired
    public AzureBlobStorageClient(Environment env) {
        this.env = env;
    }

    public void uploadBlob(String fileName, String data) throws IOException {

        /*
         * From the Azure portal, get your Storage account's name and account key.
         */
        String accountName = "juniperdevtdr";
        String accountKey = env.getProperty("env.tdr.storageAccountKey");
        String containerName = "juniper-dataset-ingest";

        /*
         * Use your Storage account's name and key to create a credential object; this is used to access your account.
         */
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);

        /*
         * From the Azure portal, get your Storage account blob service URL endpoint.
         * The URL typically looks like this:
         */
        String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);

        /*
         * Create a BlobServiceClient object that wraps the service endpoint, credential and a request pipeline.
         */
        BlobServiceClient storageClient = new BlobServiceClientBuilder().endpoint(endpoint).credential(credential).buildClient();

        /*
         * This example shows several common operations just to get you started.
         */

        /*
         * Create a client that references a to-be-created container in your Azure Storage account. This returns a
         * ContainerClient object that wraps the container's endpoint, credential and a request pipeline (inherited from storageClient).
         * Note that container names require lowercase.
         */
        BlobContainerClient blobContainerClient = storageClient.getBlobContainerClient(containerName);

        /*
         * Create a client that references a to-be-created blob in your Azure Storage account's container.
         * This returns a BlockBlobClient object that wraps the blob's endpoint, credential and a request pipeline
         * (inherited from containerClient). Note that blob names can be mixed case.
         */
        BlockBlobClient blobClient = blobContainerClient.getBlobClient(fileName).getBlockBlobClient();

        InputStream dataStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

        /*
         * Create the blob with string (plain text) content.
         */
        blobClient.upload(dataStream, data.length());

        dataStream.close();

        /*
         * Download the blob's content to output stream.
         */
        int dataSize = (int) blobClient.getProperties().getBlobSize();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(dataSize);
        blobClient.downloadStream(outputStream);
        outputStream.close();
    }
}
