package bio.terra.pearl.core.dao.datarepo;

import bio.terra.datarepo.api.JobsApi;
import bio.terra.datarepo.api.RepositoryApi;
import bio.terra.datarepo.client.ApiException;
import bio.terra.datarepo.model.*;
import bio.terra.pearl.core.shared.GoogleServiceAccountUtils;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import bio.terra.datarepo.client.ApiClient;

import java.util.*;


@Component
public class DataRepoDao {

    private GoogleCredentials serviceAccountCreds;
    private Environment env;

    @Autowired
    public DataRepoDao(Environment env) {
        this.env = env;
        serviceAccountCreds = GoogleServiceAccountUtils.parseCredentials(env.getProperty("env.tdr.pathToCreds"));
    }

    //Dataset APIs
    public JobModel createDataset(UUID spendProfileId, String datasetName) throws ApiException {
        RepositoryApi repositoryApi = getRepositoryApi();

        //TODO: AR-229. Blank schema for now, need to determine mapping of survey schema to TDR schema.
        DatasetSpecificationModel schema = new DatasetSpecificationModel();

        DatasetRequestModel dataset = new DatasetRequestModel()
                .name(datasetName)
                .defaultProfileId(spendProfileId)
                .schema(schema);

        JobModel response = repositoryApi.createDataset(dataset);

        return response;
    }

    public JobModel ingestDataset(UUID datasetId, String tableName) throws ApiException {
        RepositoryApi repositoryApi = getRepositoryApi();

        Map<String, Object> records = new HashMap<>();

        IngestRequestModel request = new IngestRequestModel()
                .table(tableName)
                //TODO: Probably want to ingest data via file, not array (otherwise this payload will be massive).
                //Need mechanism to write TDR ingest files to an Azure storage container and point TDR to that
                .format(IngestRequestModel.FormatEnum.ARRAY)
                .records(List.of(records));

        return repositoryApi.ingestDataset(datasetId, request);
    }

    public JobModel deleteDataset(UUID datasetId) throws ApiException {
        RepositoryApi repositoryApi = getRepositoryApi();

        return repositoryApi.deleteDataset(datasetId);
    }

    //Job APIs
    public JobModel getJobStatus(String jobId) throws ApiException {
        JobsApi jobsApi = getJobsApi();

        return jobsApi.retrieveJob(jobId);
    }

    public Object getJobResult(String jobId) throws ApiException {
        JobsApi jobsApi = getJobsApi();

        return jobsApi.retrieveJobResult(jobId);
    }

    //TDR API objects
    public RepositoryApi getRepositoryApi() { return new RepositoryApi(getApiClient()); }
    public JobsApi getJobsApi() { return new JobsApi(getApiClient()); }

    public ApiClient getApiClient() {
        ApiClient dataRepoClient = new ApiClient();
        AccessToken token = GoogleServiceAccountUtils.getServiceAccountToken(serviceAccountCreds);
        dataRepoClient.setBasePath(env.getProperty("env.tdr.basePath"));
        dataRepoClient.setAccessToken(token.getTokenValue());

        return dataRepoClient;
    }
}
