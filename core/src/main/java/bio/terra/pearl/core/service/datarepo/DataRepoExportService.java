package bio.terra.pearl.core.service.datarepo;

import bio.terra.datarepo.client.ApiException;
import bio.terra.datarepo.model.JobModel;
import bio.terra.datarepo.model.JobModel.JobStatusEnum;
import bio.terra.pearl.core.dao.datarepo.InitializeDatasetJobDao;
import bio.terra.pearl.core.dao.study.StudyDao;
import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.model.datarepo.InitializeDatasetJob;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.exception.StudyNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DataRepoExportService {

    private static final Logger logger = LoggerFactory.getLogger(DataRepoExportService.class);

    DataRepoClient dataRepoClient;
    Environment env;
    InitializeDatasetJobDao initializeDatasetJobDao;
    StudyEnvironmentDao studyEnvironmentDao;
    StudyDao studyDao;

    public DataRepoExportService(Environment env, DataRepoClient dataRepoClient, InitializeDatasetJobDao initializeDatasetJobDao, StudyEnvironmentDao studyEnvironmentDao, StudyDao studyDao) {
        this.env = env;
        this.dataRepoClient = dataRepoClient;
        this.initializeDatasetJobDao = initializeDatasetJobDao;
        this.studyEnvironmentDao = studyEnvironmentDao;
        this.studyDao = studyDao;
    }

    @Transactional
    public void initializeStudyEnvironmentDatasets() {
        UUID defaultSpendProfileId = UUID.fromString(env.getProperty("env.tdr.billingProfileId"));
        final String DEPLOYMENT_ZONE = "dev"; //TODO, pull from config

        //TODO: only find studyenvironments that don't have datasets initialized
        for(StudyEnvironment studyEnv : studyEnvironmentDao.findAll()) {
            Study study = studyDao.find(studyEnv.getStudyId()).orElseThrow(() -> new StudyNotFoundException(studyEnv.getStudyId()));
            String environmentName = studyEnv.getEnvironmentName().name();

            String datasetName = makeDatasetName(DEPLOYMENT_ZONE, study.getShortcode(), environmentName);

            JobModel response;
            try {
                response = dataRepoClient.createDataset(defaultSpendProfileId, datasetName);
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }

            InitializeDatasetJob job = InitializeDatasetJob.builder()
                    .studyEnvironmentId(studyEnv.getId())
                    .studyId(studyEnv.getStudyId())
                    .status(response.getJobStatus().getValue())
                    .datasetName(datasetName)
                    .tdrJobId(response.getId())
                    .build();

            initializeDatasetJobDao.create(job);
        }

    }

    public void pollRunningInitializeJobs() {
        //Query for all running TDR dataset create jobs
        List<InitializeDatasetJob> runningInitializeJobs = initializeDatasetJobDao.findAllByStatus(JobStatusEnum.RUNNING.getValue());

        logger.info("Found {} running initializeDataset jobs", runningInitializeJobs.size());

        for(InitializeDatasetJob job : runningInitializeJobs) {
            try {
                JobStatusEnum jobStatus = dataRepoClient.getJobStatus(job.getTdrJobId()).getJobStatus();

                switch(jobStatus) {
                    case SUCCEEDED -> {
                        //TODO: Resolve successful output (write back to TDR_DATASET table, and update status)
                        logger.info("initializeDataset job ID {} has succeeded.", job.getId());
                        initializeDatasetJobDao.updateJobStatus(job.getId(), JobStatusEnum.RUNNING.getValue(), jobStatus.getValue());
                    }
                    case FAILED -> {
                        logger.warn("initializeDataset job ID {} has failed.", job.getId());
                        initializeDatasetJobDao.updateJobStatus(job.getId(), JobStatusEnum.RUNNING.getValue(), jobStatus.getValue());
                    }
                    case RUNNING -> logger.info("initializeDataset job ID {} is running.", job.getId());
                    default -> logger.warn("initializeDataset job ID {} has unrecognized job status: {}", job.getId(), job.getStatus());
                }
            } catch (ApiException e) {
                throw new RuntimeException(e);
            }
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
