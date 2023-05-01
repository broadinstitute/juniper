package bio.terra.pearl.core.service.azure;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.BlockBlobItem;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.sas.SasProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Locale;

@Component
public class AzureBlobStorageClient {

    private Environment env;

    @Autowired
    public AzureBlobStorageClient(Environment env) {
        this.env = env;
    }

    public String uploadBlob(String fileName, String data) throws IOException {

        /*
         * From the Azure portal, get your Storage account's name and account key.
         */
        String accountName = env.getProperty("env.tdr.storageAccountName");
        String accountKey = env.getProperty("env.tdr.storageAccountKey");
        String containerName = env.getProperty("env.tdr.storageContainerName");

        /*
         * Use your Storage account's name and key to create a credential object; this is used to access your account.
         */
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);

        String endpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);

        /*
         * Create a BlobServiceClient object that wraps the service endpoint, credential and a request pipeline.
         */
        BlobServiceClient storageClient = new BlobServiceClientBuilder().endpoint(endpoint).credential(credential).buildClient();

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

        //Return SAS-signed URL for the uploaded TSV
        BlobSasPermission blobSasPermission = new BlobSasPermission().setReadPermission(true);
        BlobServiceSasSignatureValues builder = new BlobServiceSasSignatureValues(OffsetDateTime.now().plusDays(1), blobSasPermission).setProtocol(SasProtocol.HTTPS_ONLY);
        return String.format("https://%s.blob.core.windows.net/%s/%s?%s",blobClient.getAccountName(), blobClient.getContainerName(), blobClient.getBlobName(), blobClient.generateSas(builder));
    }
}
