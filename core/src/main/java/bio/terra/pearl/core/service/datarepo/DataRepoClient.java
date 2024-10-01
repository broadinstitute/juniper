package bio.terra.pearl.core.service.datarepo;

import bio.terra.datarepo.api.DatasetsApi;
import bio.terra.datarepo.api.JobsApi;
import bio.terra.datarepo.api.UnauthenticatedApi;
import bio.terra.datarepo.client.ApiException;
import bio.terra.datarepo.model.*;
import bio.terra.pearl.core.model.export.datarepo.TdrTable;
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
    public JobModel createDataset(UUID spendProfileId, String datasetName, String description, Set<TdrTable> tableDefinitions) throws ApiException {
        DatasetsApi datasetsApi = getDatasetsApi();

        List<TableModel> tables = tableDefinitions.stream().map(tableDefinition -> {
            List<ColumnModel> columns = tableDefinition.columns().stream().map(columnDefinition -> {
                String columnName = columnDefinition.columnName();
                TableDataType columnType = columnDefinition.dataType();
                boolean columnRequired = columnName.equalsIgnoreCase(tableDefinition.primaryKey());

                return new ColumnModel()
                        .name(columnName)
                        .datatype(columnType)
                        .required(columnRequired);
            }).toList();

            return new TableModel().name(tableDefinition.tableName()).columns(columns).primaryKey(List.of(tableDefinition.primaryKey()));
        }).toList();

        DatasetSpecificationModel schema = new DatasetSpecificationModel().tables(tables);

        DatasetRequestModel dataset = new DatasetRequestModel()
                .name(datasetName)
                .description(description)
                .cloudPlatform(CloudPlatform.AZURE)
                .defaultProfileId(spendProfileId)
                .schema(schema);

        return datasetsApi.createDataset(dataset);
    }

    /* TDR on Azure currently only supports appends during dataset ingest. As a result, this will only
        ever add rows to the dataset. Once more advanced update strategies are available, we can
        switch this over to either MERGE or REPLACE, depending on our needs.
        In a future PR, we will tackle short-term handling of this until those strategies are available,
        most likely by deleting the dataset tables and recreating them each time.
     */
    public JobModel ingestDataset(UUID spendProfileId, UUID datasetId, String tableName, String blobSasUrl) throws ApiException {
        DatasetsApi datasetsApi = getDatasetsApi();

        IngestRequestModel request = new IngestRequestModel()
                .table(tableName)
                .profileId(spendProfileId)
                .format(IngestRequestModel.FormatEnum.CSV)
                .csvFieldDelimiter("\t")
                .csvSkipLeadingRows(3) //TDR might have an off-by-one bug? Need to skip 3 rows to ignore the column name and description rows
                .updateStrategy(IngestRequestModel.UpdateStrategyEnum.APPEND) //This is the default, and the only available option on Azure right now
                .path(blobSasUrl);

        return datasetsApi.ingestDataset(datasetId, request);
    }

    public JobModel deleteDataset(UUID datasetId) throws ApiException {
        DatasetsApi datasetsApi = getDatasetsApi();

        return datasetsApi.deleteDataset(datasetId);
    }

    //TODO: This is to be replaced by JN-133. This code should only ever be executed in dev.
    public void shareWithJuniperDevs(UUID datasetId) {
        DatasetsApi datasetsApi = getDatasetsApi();

        try {
            datasetsApi.addDatasetPolicyMember(datasetId, "steward", new PolicyMemberRequest().email("juniper-dev@dev.test.firecloud.org"));
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
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
