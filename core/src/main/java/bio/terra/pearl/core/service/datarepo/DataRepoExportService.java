package bio.terra.pearl.core.service.datarepo;

import bio.terra.datarepo.client.ApiException;
import bio.terra.datarepo.model.JobModel;
import bio.terra.datarepo.model.JobModel.JobStatusEnum;
import bio.terra.pearl.core.dao.datarepo.DataRepoJobDao;
import bio.terra.pearl.core.dao.datarepo.DatasetDao;
import bio.terra.pearl.core.dao.participant.EnrolleeDao;
import bio.terra.pearl.core.dao.study.StudyDao;
import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.dao.survey.AnswerDao;
import bio.terra.pearl.core.model.datarepo.DataRepoJob;
import bio.terra.pearl.core.model.datarepo.Dataset;
import bio.terra.pearl.core.model.datarepo.JobType;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.datarepo.DatasetCreationException;
import bio.terra.pearl.core.service.exception.datarepo.DatasetNotFoundException;
import bio.terra.pearl.core.service.exception.StudyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class DataRepoExportService {

    private static final Logger logger = LoggerFactory.getLogger(DataRepoExportService.class);

    Environment env;
    DataRepoJobService dataRepoJobService;
    DatasetService datasetService;
    DataRepoClient dataRepoClient;
    AnswerDao answerDao;
    DataRepoJobDao dataRepoJobDao;
    DatasetDao datasetDao;
    EnrolleeDao enrolleeDao;
    StudyEnvironmentDao studyEnvironmentDao;
    StudyDao studyDao;

    public DataRepoExportService(Environment env,
                                 DataRepoClient dataRepoClient,
                                 DataRepoJobService dataRepoJobService,
                                 DatasetService datasetService,
                                 AnswerDao answerDao,
                                 DataRepoJobDao dataRepoJobDao,
                                 DatasetDao datasetDao,
                                 EnrolleeDao enrolleeDao,
                                 StudyDao studyDao,
                                 StudyEnvironmentDao studyEnvironmentDao) {
        this.env = env;
        this.dataRepoClient = dataRepoClient;
        this.dataRepoJobService = dataRepoJobService;
        this.answerDao = answerDao;
        this.dataRepoJobDao = dataRepoJobDao;
        this.datasetService = datasetService;
        this.datasetDao = datasetDao;
        this.enrolleeDao = enrolleeDao;
        this.studyDao = studyDao;
        this.studyEnvironmentDao = studyEnvironmentDao;
    }

    public void createDatasetsForStudyEnvironments() {
        UUID defaultSpendProfileId = UUID.fromString(Objects.requireNonNull(env.getProperty("env.tdr.billingProfileId")));
        final String DEPLOYMENT_ZONE = env.getProperty("env.tdr.deploymentZone");

        List<StudyEnvironment> allStudyEnvs = studyEnvironmentDao.findAll();
        List<UUID> studyEnvsWithDatasets = datasetDao.findAll().stream().map(Dataset::getStudyEnvironmentId).toList();
        List<StudyEnvironment> studyEnvsWithoutDatasets = allStudyEnvs.stream().filter(studyEnv -> !studyEnvsWithDatasets.contains(studyEnv.getId())).toList();

        logger.info("Found {} study environments requiring dataset creation.", studyEnvsWithoutDatasets.size());

        for(StudyEnvironment studyEnv : studyEnvsWithoutDatasets) {
            Study study = studyDao.find(studyEnv.getStudyId()).orElseThrow(() -> new StudyNotFoundException(studyEnv.getStudyId()));
            String environmentName = studyEnv.getEnvironmentName().name();

            String datasetName = makeDatasetName(DEPLOYMENT_ZONE, study.getShortcode(), environmentName);

            JobModel response;
            try {
                response = dataRepoClient.createDataset(defaultSpendProfileId, datasetName);
            } catch (ApiException e) {
                throw new DatasetCreationException(String.format("Unable to create TDR dataset for study environment %s. Error: %s", studyEnv.getStudyId(), e.getMessage()));
            }

            DataRepoJob job = DataRepoJob.builder()
                    .studyEnvironmentId(studyEnv.getId())
                    .status(response.getJobStatus().getValue())
                    .datasetName(datasetName)
                    .tdrJobId(response.getId())
                    .jobType(JobType.CREATE_DATASET)
                    .build();

            dataRepoJobService.create(job);
        }

    }

    public void ingestDatasets() {
        List<Dataset> outdatedDatasets = datasetDao.findAll().stream().filter(dataset -> dataset.getLastExported().isBefore(Instant.now().minus(4, ChronoUnit.HOURS))).toList();

        logger.info("Found {} study environments requiring dataset ingest", outdatedDatasets.size());

        for(Dataset dataset : outdatedDatasets) {
            logger.info("Ingesting data for study environment ID {}", dataset.getStudyEnvironmentId());
            ingestDataForStudyEnvironment(dataset);
        }
    }

    public void ingestDataForStudyEnvironment(Dataset studyEnvDataset) {
        try {
            JobModel ingestJob = dataRepoClient.ingestDataset(studyEnvDataset.getDatasetId(), "enrollee");
            logger.info("Ingest job returned with job ID {}", ingestJob.getId());
            //Store in DB
            DataRepoJob job = DataRepoJob.builder()
                    .studyEnvironmentId(studyEnvDataset.getStudyEnvironmentId())
                    .status(ingestJob.getJobStatus().getValue())
                    .datasetName(studyEnvDataset.getDatasetName())
                    .tdrJobId(ingestJob.getId())
                    .jobType(JobType.INGEST_DATASET)
                    .build();

            dataRepoJobService.create(job);
        } catch (ApiException e) {
            logger.error("Unable to ingest dataset {} for study env {}. Error: {}", studyEnvDataset.getDatasetId(), studyEnvDataset.getStudyEnvironmentId(), e.getMessage());
        }
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

                    logger.info("createDataset job ID {} has succeeded. Dataset {} has been created.", job.getId(), job.getDatasetName());
                    Dataset dataset = Dataset.builder()
                            .studyEnvironmentId(job.getStudyEnvironmentId())
                            .datasetId(UUID.fromString(jobResult.get("id").toString()))
                            .datasetName(job.getDatasetName())
                            .lastExported(Instant.ofEpochSecond(0))
                            .build();

                    datasetService.create(dataset);
                    dataRepoJobService.updateJobStatus(job.getId(), jobStatus.getValue());
                }
                case FAILED -> {
                    logger.warn("createDataset job ID {} has failed. Dataset {} failed to create.", job.getId(), job.getDatasetName());
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
                    LinkedHashMap<String, Object> jobResult = (LinkedHashMap<String, Object>) dataRepoClient.getJobResult(job.getTdrJobId());
                    UUID dataRepoId = UUID.fromString(jobResult.get("dataset_id").toString());

                    Dataset dataset = datasetService.findByDataRepoId(dataRepoId).orElseThrow(() -> new DatasetNotFoundException(dataRepoId));

                    logger.info("ingestDataset job ID {} has succeeded. Dataset {} successfully ingested.", job.getId(), job.getDatasetName());
                    dataRepoJobService.updateJobStatus(job.getId(), jobStatus.getValue());
                    datasetService.updateLastExported(dataset.getId(), Instant.now());
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

    public boolean getServiceStatus() {
        try {
            return dataRepoClient.getServiceStatus().isOk();
        } catch (ApiException e) {
            return false;
        }
    }

    public String makeDatasetName(String deploymentZone, String studyName, String environmentName) {
        if(deploymentZone.equalsIgnoreCase("prod"))
            return String.format("d2p_%s_%s", studyName, environmentName);
        else
            return String.format("d2p_%s_%s_%s", deploymentZone, studyName, environmentName);
    }

}
