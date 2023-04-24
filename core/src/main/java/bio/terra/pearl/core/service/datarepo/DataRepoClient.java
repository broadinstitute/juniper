package bio.terra.pearl.core.service.datarepo;

import bio.terra.datarepo.api.DatasetsApi;
import bio.terra.datarepo.api.JobsApi;
import bio.terra.datarepo.api.RepositoryApi;
import bio.terra.datarepo.api.UnauthenticatedApi;
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
public class DataRepoClient {

    private Environment env;

    @Autowired
    public DataRepoClient(Environment env) {
        this.env = env;
    }

    //Dataset APIs
    public JobModel createDataset(UUID spendProfileId, String datasetName) throws ApiException {
        DatasetsApi datasetsApi = getDatasetsApi();

        //TODO: AR-229. Placeholder schema for now, need to determine mapping of survey schema to TDR schema.
        DatasetSpecificationModel schema = new DatasetSpecificationModel()
                .tables(List.of(new TableModel().name("enrollee").columns(List.of(
                        new ColumnModel().name("shortcode").datatype(TableDataType.STRING)
                ))));

        DatasetRequestModel dataset = new DatasetRequestModel()
                .name(datasetName)
                .cloudPlatform(CloudPlatform.AZURE)
                .defaultProfileId(spendProfileId)
                .schema(schema);

        JobModel response = datasetsApi.createDataset(dataset);

        return response;
    }

    public JobModel ingestDataset(UUID datasetId, String tableName) throws ApiException {
        DatasetsApi datasetsApi = getDatasetsApi();

        Map<String, Object> records = new HashMap<>();

        //TODO: Once we have the TSV prepared for export, we can either convert it into a TDR array model (short term solution),
        // or upload it to an Azure storage container elsewhere and point to that URL here. For now, just add a new row each time
        // to see that the flow is working.
        records.put("shortcode", "TESTUSER");

        IngestRequestModel request = new IngestRequestModel()
                .table(tableName)
                //TODO: Probably want to ingest data via file, not array (otherwise this payload will be massive).
                //Need mechanism to write TDR ingest files to an Azure storage container and point TDR to that
                .format(IngestRequestModel.FormatEnum.ARRAY)
                .records(List.of(records));

        return datasetsApi.ingestDataset(datasetId, request);
    }

    public JobModel deleteDataset(UUID datasetId) throws ApiException {
        DatasetsApi datasetsApi = getDatasetsApi();

        return datasetsApi.deleteDataset(datasetId);
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

    //Unauthenticated APIs
    public RepositoryStatusModel getServiceStatus() throws ApiException{
        UnauthenticatedApi unauthenticatedApi = getUnauthenticatedApi();

        return unauthenticatedApi.serviceStatus();
    }

    //TDR API objects
    private DatasetsApi getDatasetsApi() { return new DatasetsApi(getApiClient()); }
    private JobsApi getJobsApi() { return new JobsApi(getApiClient()); }
    private UnauthenticatedApi getUnauthenticatedApi() { return new UnauthenticatedApi(getApiClient()); }

    private ApiClient getApiClient() {
        ApiClient dataRepoClient = new ApiClient();
        GoogleCredentials serviceAccountCreds = GoogleServiceAccountUtils.parseCredentials(env.getProperty("env.tdr.serviceAccountCreds"));
        AccessToken token = GoogleServiceAccountUtils.getServiceAccountToken(serviceAccountCreds);
        dataRepoClient.setBasePath(env.getProperty("env.tdr.basePath"));
        dataRepoClient.setAccessToken(token.getTokenValue());

        return dataRepoClient;
    }
}
