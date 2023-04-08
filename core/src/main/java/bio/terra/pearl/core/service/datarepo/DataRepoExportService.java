package bio.terra.pearl.core.service.datarepo;

import bio.terra.datarepo.client.ApiException;
import bio.terra.datarepo.model.EnumerateDatasetModel;
import bio.terra.datarepo.model.JobModel;
import bio.terra.datarepo.model.JobModel.JobStatusEnum;
import bio.terra.pearl.core.dao.datarepo.InitializeDatasetJobDao;
import bio.terra.pearl.core.dao.datarepo.TdrDatasetDao;
import bio.terra.pearl.core.dao.study.StudyDao;
import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.model.datarepo.InitializeDatasetJob;
import bio.terra.pearl.core.model.datarepo.TdrDataset;
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
    TdrDatasetDao tdrDatasetDao;
    StudyEnvironmentDao studyEnvironmentDao;
    StudyDao studyDao;

    public DataRepoExportService(Environment env,
                                 DataRepoClient dataRepoClient,
                                 InitializeDatasetJobDao initializeDatasetJobDao,
                                 StudyEnvironmentDao studyEnvironmentDao,
                                 StudyDao studyDao,
                                 TdrDatasetDao tdrDatasetDao) {
        this.env = env;
        this.dataRepoClient = dataRepoClient;
        this.initializeDatasetJobDao = initializeDatasetJobDao;
        this.tdrDatasetDao = tdrDatasetDao;
        this.studyEnvironmentDao = studyEnvironmentDao;
        this.studyDao = studyDao;
    }

    @Transactional
    public void initializeStudyEnvironmentDatasets() {
        UUID defaultSpendProfileId = UUID.fromString(env.getProperty("env.tdr.billingProfileId"));
        final String DEPLOYMENT_ZONE = "mbemis"; //TODO, pull from config

        List<StudyEnvironment> allStudyEnvs = studyEnvironmentDao.findAll();

        List<UUID> studyEnvsWithDatasets = tdrDatasetDao.findAll().stream().map(TdrDataset::getStudyEnvironmentId).toList();

        List<StudyEnvironment> studyEnvsToInitialize = allStudyEnvs.stream().filter(studyEnv -> !studyEnvsWithDatasets.contains(studyEnv.getId())).toList();
        logger.info("Found {} datasets to initialize.", studyEnvsToInitialize.size());

        //TODO: only find studyenvironments that don't have datasets initialized
        for(StudyEnvironment studyEnv : studyEnvsToInitialize) {
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

    @Transactional
    public void pollRunningInitializeJobs() {
        //Query for all running TDR dataset create jobs
        List<InitializeDatasetJob> runningInitializeJobs = initializeDatasetJobDao.findAllByStatus(JobStatusEnum.RUNNING.getValue());

        logger.info("Found {} running initializeDataset jobs", runningInitializeJobs.size());

        for(InitializeDatasetJob job : runningInitializeJobs) {
            try {
                JobStatusEnum jobStatus = dataRepoClient.getJobStatus(job.getTdrJobId()).getJobStatus();

                switch(jobStatus) {
                    case SUCCEEDED -> {
                        logger.info("initializeDataset job ID {} has succeeded. Dataset {} has been created.", job.getId(), job.getDatasetName());
                        TdrDataset dataset = TdrDataset.builder()
                                .studyEnvironmentId(job.getStudyEnvironmentId())
                                .studyId(job.getStudyId())
                                .datasetId(UUID.randomUUID()) //TODO: pull this out of getJobResult response
                                .datasetName(job.getDatasetName())
                                .build();

                        tdrDatasetDao.create(dataset);
                        initializeDatasetJobDao.updateJobStatus(job.getId(), JobStatusEnum.RUNNING.getValue(), jobStatus.getValue());
                    }
                    case FAILED -> {
                        logger.warn("initializeDataset job ID {} has failed. Dataset {} failed to create.", job.getId(), job.getDatasetName());
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

    public EnumerateDatasetModel listDatasets() {
        try {
            return dataRepoClient.enumerateDatasets();
        } catch (ApiException e) {
            throw new RuntimeException(e);
        }
    }

    public String deleteDataset(UUID datasetId) {
        try {
            return dataRepoClient.deleteDataset(datasetId).getId();
        } catch (ApiException e) {
            throw new RuntimeException(e);
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
