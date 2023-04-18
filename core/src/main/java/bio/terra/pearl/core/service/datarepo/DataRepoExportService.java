package bio.terra.pearl.core.service.datarepo;

import bio.terra.datarepo.client.ApiException;
import bio.terra.datarepo.model.JobModel;
import bio.terra.datarepo.model.JobModel.JobStatusEnum;
import bio.terra.pearl.core.dao.datarepo.CreateDatasetJobDao;
import bio.terra.pearl.core.dao.datarepo.DatasetDao;
import bio.terra.pearl.core.dao.study.StudyDao;
import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.model.datarepo.CreateDatasetJob;
import bio.terra.pearl.core.model.datarepo.Dataset;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.DatasetCreationException;
import bio.terra.pearl.core.service.exception.StudyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class DataRepoExportService {

    private static final Logger logger = LoggerFactory.getLogger(DataRepoExportService.class);

    Environment env;
    CreateDatasetJobService createDatasetJobService;
    DatasetService datasetService;
    DataRepoClient dataRepoClient;
    CreateDatasetJobDao createDatasetJobDao;
    DatasetDao datasetDao;
    StudyEnvironmentDao studyEnvironmentDao;
    StudyDao studyDao;

    public DataRepoExportService(Environment env,
                                 DataRepoClient dataRepoClient,
                                 CreateDatasetJobService createDatasetJobService,
                                 DatasetService datasetService,
                                 DatasetDao datasetDao,
                                 CreateDatasetJobDao createDatasetJobDao,
                                 StudyDao studyDao,
                                 StudyEnvironmentDao studyEnvironmentDao) {
        this.env = env;
        this.dataRepoClient = dataRepoClient;
        this.createDatasetJobService = createDatasetJobService;
        this.datasetService = datasetService;
        this.createDatasetJobDao = createDatasetJobDao;
        this.datasetDao = datasetDao;
        this.studyDao = studyDao;
        this.studyEnvironmentDao = studyEnvironmentDao;
    }

    public void createDatasetsForStudyEnvironments() {
        UUID defaultSpendProfileId = UUID.fromString(Objects.requireNonNull(env.getProperty("env.tdr.billingProfileId")));
        final String DEPLOYMENT_ZONE = env.getProperty("env.deploymentZone");

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
                throw new DatasetCreationException(String.format("Unable to create TDR dataset for study environment %s", studyEnv.getStudyId()));
            }

            CreateDatasetJob job = CreateDatasetJob.builder()
                    .studyEnvironmentId(studyEnv.getId())
                    .status(response.getJobStatus().getValue())
                    .datasetName(datasetName)
                    .tdrJobId(response.getId())
                    .build();

            createDatasetJobService.create(job);
        }

    }

    public void pollRunningCreateDatasetJobs() {
        //Query for all running TDR dataset create jobs
        List<CreateDatasetJob> runningCreateJobs = createDatasetJobDao.findAllByStatus(JobStatusEnum.RUNNING.getValue());

        logger.info("Found {} running createDataset jobs", runningCreateJobs.size());

        //For each running job, query TDR for the latest status
        for(CreateDatasetJob job : runningCreateJobs) {
            pollAndUpdateJobStatus(job);
        }
    }

    private void pollAndUpdateJobStatus(CreateDatasetJob job) {
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
                            .build();

                    datasetService.create(dataset);
                    createDatasetJobService.updateJobStatus(job.getId(), jobStatus.getValue());
                }
                case FAILED -> {
                    logger.warn("createDataset job ID {} has failed. Dataset {} failed to create.", job.getId(), job.getDatasetName());
                    createDatasetJobDao.updateJobStatus(job.getId(), jobStatus.getValue());
                }
                case RUNNING -> logger.info("createDataset job ID {} is running.", job.getId());
                default -> logger.warn("createDataset job ID {} has unrecognized job status: {}", job.getId(), job.getStatus());
            }
        } catch (ApiException e) {
            logger.error("Unable to get TDR job status for job ID {}", job.getTdrJobId());
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
