package bio.terra.pearl.core.service.datarepo;

import bio.terra.datarepo.client.ApiException;
import bio.terra.datarepo.model.JobModel;
import bio.terra.datarepo.model.JobModel.JobStatusEnum;
import bio.terra.pearl.core.dao.datarepo.DataRepoJobDao;
import bio.terra.pearl.core.dao.datarepo.DatasetDao;
import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.dao.study.PortalStudyDao;
import bio.terra.pearl.core.dao.study.StudyDao;
import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.dao.survey.AnswerDao;
import bio.terra.pearl.core.model.admin.AdminUser;
import bio.terra.pearl.core.model.datarepo.*;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.azure.AzureBlobStorageClient;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.exception.datarepo.DatasetCreationException;
import bio.terra.pearl.core.service.exception.datarepo.DatasetDeletionException;
import bio.terra.pearl.core.service.exception.datarepo.DatasetNotFoundException;
import bio.terra.pearl.core.service.export.*;
import bio.terra.pearl.core.service.export.EnrolleeExportService;
import bio.terra.pearl.core.service.export.ExportFileFormat;
import bio.terra.pearl.core.service.export.instance.ExportOptions;
import bio.terra.pearl.core.service.export.instance.ModuleExportInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.Instant;
import java.util.*;

@Service
public class DataRepoExportService {

    private static final Logger logger = LoggerFactory.getLogger(DataRepoExportService.class);

    Environment env;
    AzureBlobStorageClient azureBlobStorageClient;
    DataRepoJobService dataRepoJobService;
    DatasetService datasetService;
    DataRepoClient dataRepoClient;
    EnrolleeExportService enrolleeExportService;
    AnswerDao answerDao;
    DataRepoJobDao dataRepoJobDao;
    DatasetDao datasetDao;
    EnrolleeDao enrolleeDao;
    PortalStudyDao portalStudyDao;
    StudyEnvironmentDao studyEnvironmentDao;
    StudyDao studyDao;

    public DataRepoExportService(Environment env,
                                 AzureBlobStorageClient azureBlobStorageClient,
                                 DataRepoClient dataRepoClient,
                                 DataRepoJobService dataRepoJobService,
                                 DatasetService datasetService,
                                 EnrolleeExportService enrolleeExportService,
                                 AnswerDao answerDao,
                                 DataRepoJobDao dataRepoJobDao,
                                 DatasetDao datasetDao,
                                 EnrolleeDao enrolleeDao,
                                 PortalStudyDao portalStudyDao,
                                 StudyDao studyDao,
                                 StudyEnvironmentDao studyEnvironmentDao) {
        this.env = env;
        this.azureBlobStorageClient = azureBlobStorageClient;
        this.dataRepoClient = dataRepoClient;
        this.dataRepoJobService = dataRepoJobService;
        this.enrolleeExportService = enrolleeExportService;
        this.answerDao = answerDao;
        this.dataRepoJobDao = dataRepoJobDao;
        this.datasetService = datasetService;
        this.datasetDao = datasetDao;
        this.enrolleeDao = enrolleeDao;
        this.portalStudyDao = portalStudyDao;
        this.studyDao = studyDao;
        this.studyEnvironmentDao = studyEnvironmentDao;
    }

    public List<Dataset> listDatasetsForStudyEnvironment(UUID studyEnvironmentId) {
        return datasetDao.findByStudyEnvironmentId(studyEnvironmentId);
    }

    public Dataset getDatasetByName(String datasetName) {
        return datasetDao.findByDatasetName(datasetName).orElseThrow(() -> new DatasetNotFoundException(datasetName));
    }

    public List<DataRepoJob> getJobHistoryForDataset(UUID datasetId) {
        return dataRepoJobDao.findByDatasetId(datasetId);
    }

    public void createDataset(StudyEnvironment studyEnv, String datasetName, String description, AdminUser user) {
        //TODO: JN-125: This default spend profile is temporary. Eventually, we will want to configure spend profiles
        // on a per-study basis and store those in the Juniper DB.
        UUID defaultSpendProfileId = UUID.fromString(Objects.requireNonNull(env.getProperty("env.tdr.billingProfileId")));

        Set<TdrTable> tableDefinitions = Set.of(new TdrTable("enrollee", "enrollee_shortcode", generateDatasetSchema(studyEnv.getId())));

        JobModel response;
        try {
            response = dataRepoClient.createDataset(defaultSpendProfileId, datasetName, description, tableDefinitions);
        } catch (ApiException e) {
            throw new DatasetCreationException(String.format("Unable to create TDR dataset for study environment %s. Error: %s", studyEnv.getStudyId(), e.getMessage()));
        }

        Dataset dataset = Dataset.builder()
                .status(DatasetStatus.CREATING)
                .datasetName(datasetName)
                .description(description)
                .createdBy(user.getId())
                .lastExported(Instant.ofEpochSecond(0))
                .studyEnvironmentId(studyEnv.getId())
                .build();

        Dataset createdDataset = datasetService.create(dataset);

        DataRepoJob job = DataRepoJob.builder()
                .studyEnvironmentId(studyEnv.getId())
                .status(response.getJobStatus().getValue())
                .datasetName(datasetName)
                .datasetId(createdDataset.getId())
                .tdrJobId(response.getId())
                .jobType(JobType.CREATE_DATASET)
                .build();

        dataRepoJobService.create(job);
    }

    @Transactional
    public void deleteDataset(StudyEnvironment studyEnv, String datasetName) {
        Dataset dataset = datasetDao.findByDatasetName(datasetName).orElseThrow(() -> new DatasetNotFoundException("Dataset not found."));

        List<DatasetStatus> deletableStatuses = List.of(DatasetStatus.CREATED, DatasetStatus.FAILED);
        if(!deletableStatuses.contains(dataset.getStatus())) {
            throw new DatasetDeletionException(String.format("Could not delete dataset %s. Dataset was in %s state, but must be in a terminal state.", datasetName, dataset.getStatus()));
        }

        JobModel response;
        try {
            response = dataRepoClient.deleteDataset(dataset.getTdrDatasetId());
        } catch (ApiException e) {
            throw new DatasetDeletionException(String.format("Could not delete dataset %s. TDR dataset failed to delete. Error: %s", datasetName, e.getMessage()));
        }

        DataRepoJob job = DataRepoJob.builder()
                .studyEnvironmentId(studyEnv.getId())
                .status(response.getJobStatus().getValue())
                .datasetId(dataset.getId())
                .datasetName(datasetName)
                .tdrJobId(response.getId())
                .jobType(JobType.DELETE_DATASET)
                .build();

        dataRepoJobService.create(job);
        datasetService.updateStatus(dataset.getId(), DatasetStatus.DELETING);
    }

    public String uploadCsvToAzureStorage(UUID studyEnvironmentId, UUID datasetId) {
        ExportOptions exportOptions = new ExportOptions(false, false, false, ExportFileFormat.TSV, null);

        //Even though this is actually formatted as a TSV, TDR only accepts files ending in .csv or .json.
        //In the DataRepoClient call, we specify that the CSV delimiter is "\t", which will make it all work fine.
        String blobName = datasetId.toString() + "_" + studyEnvironmentId + "_" + Instant.now() + ".csv";

        //Backtrack from studyEnvironmentId to get the portalId, so we can export the study environment data
        StudyEnvironment studyEnv = studyEnvironmentDao.find(studyEnvironmentId).orElseThrow(() -> new NotFoundException("Study environment not found."));
        PortalStudy portalStudy = portalStudyDao.findByStudyId(studyEnv.getStudyId()).stream().findFirst().orElseThrow(() -> new NotFoundException("Portal study not found."));

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            enrolleeExportService.export(exportOptions, portalStudy.getPortalId(), studyEnvironmentId, outputStream);
            outputStream.close();

            String exportData = outputStream.toString();
            return azureBlobStorageClient.uploadBlobAndSignUrl(blobName, exportData);
        } catch (Exception e) {
            throw new RuntimeException("Could not export and upload CSV for TDR ingest. Error: " + e.getMessage());
        }
    }

    public void ingestDataForStudyEnvironment(UUID datasetId) {
        Dataset dataset = datasetService.findById(datasetId).get();

        UUID defaultSpendProfileId = UUID.fromString(Objects.requireNonNull(env.getProperty("env.tdr.billingProfileId")));
        String blobSasUrl = uploadCsvToAzureStorage(dataset.getStudyEnvironmentId(), dataset.getId());

        try {
            JobModel ingestJob = dataRepoClient.ingestDataset(defaultSpendProfileId, dataset.getTdrDatasetId(), "enrollee", blobSasUrl);
            logger.info("Ingest job returned with job ID {}", ingestJob.getId());
            //Store in DB
            DataRepoJob job = DataRepoJob.builder()
                    .studyEnvironmentId(dataset.getStudyEnvironmentId())
                    .status(ingestJob.getJobStatus().getValue())
                    .datasetId(dataset.getId())
                    .datasetName(dataset.getDatasetName())
                    .tdrJobId(ingestJob.getId())
                    .jobType(JobType.INGEST_DATASET)
                    .build();

            dataRepoJobService.create(job);
        } catch (ApiException e) {
            logger.error("Unable to ingest dataset {} for study env {}. Error: {}", dataset.getId(), dataset.getStudyEnvironmentId(), e.getMessage());
        }
    }

    public Set<TdrColumn> generateDatasetSchema(UUID studyEnvironmentId) {
        ExportOptions exportOptions = new ExportOptions(false, false, false, ExportFileFormat.TSV, null);

        //Backtrack from studyEnvironmentId to get the portalId, so we can export the study environment data
        StudyEnvironment studyEnv = studyEnvironmentDao.find(studyEnvironmentId).orElseThrow(() -> new NotFoundException("Study environment not found."));
        PortalStudy portalStudy = portalStudyDao.findByStudyId(studyEnv.getStudyId()).stream().findFirst().orElseThrow(() -> new NotFoundException("Portal study not found."));

        Set<TdrColumn> tdrColumns = new LinkedHashSet<>();

        try {
            List<ModuleExportInfo> moduleExportInfos = enrolleeExportService.generateModuleInfos(exportOptions, portalStudy.getPortalId(), studyEnvironmentId);
            List<Map<String, String>> enrolleeMaps = enrolleeExportService.generateExportMaps(portalStudy.getPortalId(), studyEnvironmentId,
                    moduleExportInfos, exportOptions.limit());

            TsvExporter tsvExporter = new TsvExporter(moduleExportInfos, enrolleeMaps);

            tsvExporter.applyToEveryColumn((moduleExportInfo, itemExportInfo, isOtherDescription) -> tdrColumns.add(new TdrColumn(
                    DataRepoExportUtils.juniperToDataRepoColumnName(moduleExportInfo.getFormatter().getColumnKey(moduleExportInfo, itemExportInfo, isOtherDescription, null)),
                    DataRepoExportUtils.juniperToDataRepoColumnType(itemExportInfo.getDataType())
                )
            ));
        } catch (Exception e) {
            throw new RuntimeException("Could not generate dataset schema for study environment " + studyEnvironmentId + ". Error: " + e.getMessage());
        }

        return tdrColumns;
    }

    public void pollRunningJobs() {
        //Query for all running TDR jobs
        List<DataRepoJob> runningJobs = dataRepoJobDao.findAllByStatus(JobStatusEnum.RUNNING.getValue());

        logger.info("Found {} running TDR jobs", runningJobs.size());

        //For each running job, query TDR for the latest status
        for(DataRepoJob job : runningJobs) {
            switch(job.getJobType()) {
                case CREATE_DATASET -> pollAndUpdateCreateJobStatus(job);
                case INGEST_DATASET -> pollAndUpdateIngestJobStatus(job);
                case DELETE_DATASET -> pollAndUpdateDeleteJobStatus(job);
                default -> logger.error("Unknown job type '{}' for TDR job {}.", job.getJobType(), job.getTdrJobId());
            }
        }
    }

    private void pollAndUpdateCreateJobStatus(DataRepoJob job) {
        try {
            JobStatusEnum jobStatus = dataRepoClient.getJobStatus(job.getTdrJobId()).getJobStatus();

            switch(jobStatus) {
                case SUCCEEDED -> {
                    LinkedHashMap<String, Object> jobResult = (LinkedHashMap<String, Object>) dataRepoClient.getJobResult(job.getTdrJobId());
                    UUID tdrDatasetId = UUID.fromString(jobResult.get("id").toString());

                    logger.info("createDataset job ID {} has succeeded. Dataset {} has been created.", job.getId(), job.getDatasetName());

                    datasetService.setTdrDatasetId(job.getDatasetId(), tdrDatasetId);
                    datasetService.updateStatus(job.getDatasetId(), DatasetStatus.CREATED);
                    dataRepoJobService.updateJobStatus(job.getId(), jobStatus.getValue());

                    //TODO: This is to be replaced by JN-133. This code should only ever be executed in dev.
                    // The ultimate safeguard here is in Terra, where this would fail in production because
                    // the juniper-dev managed group does not exist in Terra Prod. But this is an extra layer
                    // of safety to be totally safe.
                    String DEPLOYMENT_ZONE = env.getProperty("env.tdr.deploymentZone");
                    if(!DEPLOYMENT_ZONE.equalsIgnoreCase("prod")) {
                        logger.info("Sharing dataset with Juniper dev team. If you're seeing this in prod, panic!");
                        dataRepoClient.shareWithJuniperDevs(tdrDatasetId);
                    }

                    ingestDataForStudyEnvironment(job.getDatasetId());
                }
                case FAILED -> {
                    logger.warn("createDataset job ID {} has failed. Dataset {} failed to create.", job.getId(), job.getDatasetName());

                    datasetService.updateStatus(job.getDatasetId(), DatasetStatus.FAILED);
                    dataRepoJobService.updateJobStatus(job.getId(), jobStatus.getValue());
                }
                case RUNNING -> logger.info("createDataset job ID {} is running.", job.getId());
                default -> logger.warn("createDataset job ID {} has unrecognized job status: {}", job.getId(), job.getStatus());
            }
        } catch (ApiException e) {
            logger.error("Unable to get TDR job status for job ID {}", job.getTdrJobId());
        }
    }

    private void pollAndUpdateIngestJobStatus(DataRepoJob job) {
        try {
            JobStatusEnum jobStatus = dataRepoClient.getJobStatus(job.getTdrJobId()).getJobStatus();

            switch(jobStatus) {
                case SUCCEEDED -> {
                    logger.info("ingestDataset job ID {} has succeeded. Dataset {} successfully ingested.", job.getId(), job.getDatasetName());
                    dataRepoJobService.updateJobStatus(job.getId(), jobStatus.getValue());
                    datasetService.updateLastExported(job.getDatasetId(), Instant.now());
                }
                case FAILED -> {
                    logger.warn("ingestDataset job ID {} has failed. Dataset {} failed to ingest.", job.getId(), job.getDatasetName());
                    dataRepoJobService.updateJobStatus(job.getId(), jobStatus.getValue());
                }
                case RUNNING -> logger.info("ingestDataset job ID {} is running.", job.getId());
                default -> logger.warn("ingestDataset job ID {} has unrecognized job status: {}", job.getId(), job.getStatus());
            }
        } catch (ApiException e) {
            logger.error("Unable to get TDR job status for job ID {}. Error: {}", job.getTdrJobId(), e.getMessage());
        }
    }

    private void pollAndUpdateDeleteJobStatus(DataRepoJob job) {
        try {
            JobStatusEnum jobStatus = dataRepoClient.getJobStatus(job.getTdrJobId()).getJobStatus();

            switch(jobStatus) {
                case SUCCEEDED -> {
                    Dataset dataset = datasetService.findById(job.getDatasetId()).orElseThrow(() -> new DatasetNotFoundException(job.getDatasetId()));

                    logger.info("deleteDataset job ID {} has succeeded. Dataset {} successfully deleted.", job.getId(), job.getDatasetId());
                    dataRepoJobService.updateJobStatus(job.getId(), jobStatus.getValue());
                    dataRepoJobService.deleteByDatasetId(dataset.getId());
                    datasetService.delete(dataset);
                }
                case FAILED -> {
                    logger.warn("deleteDataset job ID {} has failed. Dataset {} failed to delete.", job.getId(), job.getDatasetId());
                    dataRepoJobService.updateJobStatus(job.getId(), jobStatus.getValue());
                }
                case RUNNING -> logger.info("deleteDataset job ID {} is running.", job.getId());
                default -> logger.warn("deleteDataset job ID {} has unrecognized job status: {}", job.getId(), job.getStatus());
            }
        } catch (ApiException e) {
            logger.error("Unable to get TDR job status for job ID {}. Error: {}", job.getTdrJobId(), e.getMessage());
        }
    }

    public boolean getServiceStatus() {
        try {
            return dataRepoClient.getServiceStatus().isOk();
        } catch (ApiException e) {
            return false;
        }
    }

}
