package bio.terra.pearl.core.service.azure;

import com.azure.storage.blob.*;
import com.azure.storage.blob.sas.BlobSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.blob.specialized.BlockBlobClient;
import com.azure.storage.common.StorageSharedKeyCredential;
import com.azure.storage.common.sas.SasProtocol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
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

    public String uploadBlob(String blobName, String data) throws IOException {
        String accountName = env.getProperty("env.tdr.storageAccountName");
        String accountKey = env.getProperty("env.tdr.storageAccountKey");
        String containerName = env.getProperty("env.tdr.storageContainerName");

        //Get a credential object to access the storage container
        StorageSharedKeyCredential credential = new StorageSharedKeyCredential(accountName, accountKey);

        //Create a BlobServiceClient object that wraps the service endpoint, credential and a request pipeline.
        String storageClientEndpoint = String.format(Locale.ROOT, "https://%s.blob.core.windows.net", accountName);
        BlobServiceClient storageClient = new BlobServiceClientBuilder().endpoint(storageClientEndpoint).credential(credential).buildClient();

        //Create a client that references the storage container
        BlobContainerClient blobContainerClient = storageClient.getBlobContainerClient(containerName);

        //Create a client that references the to-be-created blob in the storage container
        BlockBlobClient blobClient = blobContainerClient.getBlobClient(blobName).getBlockBlobClient();

        InputStream dataStream = new ByteArrayInputStream(data.getBytes(StandardCharsets.UTF_8));

        //Create the blob
        blobClient.upload(dataStream, data.length());

        dataStream.close();

        //Generate a SAS-signed URL that is good for 1 hour (ingest should be much quicker than this)
        BlobSasPermission blobSasPermission = new BlobSasPermission().setReadPermission(true);
        BlobServiceSasSignatureValues builder = new BlobServiceSasSignatureValues(OffsetDateTime.now().plusHours(1), blobSasPermission).setProtocol(SasProtocol.HTTPS_ONLY);

        //Return SAS-signed URL for the uploaded TSV
        return String.format("https://%s.blob.core.windows.net/%s/%s?%s",
                blobClient.getAccountName(),
                blobClient.getContainerName(),
                blobClient.getBlobName(),
                blobClient.generateSas(builder));
    }
}
