package bio.terra.pearl.core.service.study;

import bio.terra.pearl.core.dao.participant.WithdrawnEnrolleeDao;
import bio.terra.pearl.core.dao.study.StudyEnvironmentDao;
import bio.terra.pearl.core.dao.study.StudyEnvironmentSurveyDao;
import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.ListChange;
import bio.terra.pearl.core.model.publishing.StudyEnvironmentChange;
import bio.terra.pearl.core.model.publishing.VersionedConfigChange;
import bio.terra.pearl.core.model.publishing.VersionedEntityChange;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import bio.terra.pearl.core.service.export.dataimport.ImportService;
import bio.terra.pearl.core.service.datarepo.DataRepoJobService;
import bio.terra.pearl.core.service.datarepo.DatasetService;
import bio.terra.pearl.core.service.exception.NotFoundException;
import bio.terra.pearl.core.service.export.integration.ExportIntegrationService;
import bio.terra.pearl.core.service.kit.StudyEnvironmentKitTypeService;
import bio.terra.pearl.core.service.notification.TriggerService;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.FamilyEnrolleeService;
import bio.terra.pearl.core.service.participant.FamilyService;
import bio.terra.pearl.core.service.publishing.PortalEnvPublishable;
import bio.terra.pearl.core.service.publishing.PublishingUtils;
import bio.terra.pearl.core.service.publishing.StudyEnvPublishable;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.core.service.workflow.ParticipantDataChangeService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/** This only implements StudyEnvPublishable to handle pre-enroll survey changes, since those are directly attached.  Everything else
 * is published from its own service */
@Service
public class StudyEnvironmentService extends CrudService<StudyEnvironment, StudyEnvironmentDao> implements StudyEnvPublishable {
    private final FamilyService familyService;
    private final FamilyEnrolleeService familyEnrolleeService;
    private final EnrolleeRelationService enrolleeRelationService;
    private final ParticipantDataChangeService participantDataChangeService;
    private final StudyEnvironmentSurveyDao studyEnvironmentSurveyDao;
    private final StudyEnvironmentConfigService studyEnvironmentConfigService;
    private final EnrolleeService enrolleeService;
    private final PreEnrollmentResponseDao preEnrollmentResponseDao;
    private final TriggerService triggerService;
    private final WithdrawnEnrolleeDao withdrawnEnrolleeDao;
    private final StudyEnvironmentKitTypeService studyEnvironmentKitTypeService;
    private final ImportService importService;
    private final ExportIntegrationService exportIntegrationService;
    private final SurveyService surveyService;


    public StudyEnvironmentService(StudyEnvironmentDao studyEnvironmentDao,
                                   StudyEnvironmentSurveyDao studyEnvironmentSurveyDao,
                                   StudyEnvironmentConfigService studyEnvironmentConfigService,
                                   EnrolleeService enrolleeService,
                                   PreEnrollmentResponseDao preEnrollmentResponseDao,
                                   TriggerService triggerService,
                                   WithdrawnEnrolleeDao withdrawnEnrolleeDao,
                                   StudyEnvironmentKitTypeService studyEnvironmentKitTypeService,
                                   ImportService importService, FamilyService familyService,
                                   FamilyEnrolleeService familyEnrolleeService,
                                   EnrolleeRelationService enrolleeRelationService,
                                   ParticipantDataChangeService participantDataChangeService,
                                   @Lazy ExportIntegrationService exportIntegrationService, SurveyService surveyService) {
        super(studyEnvironmentDao);
        this.studyEnvironmentSurveyDao = studyEnvironmentSurveyDao;
        this.studyEnvironmentConfigService = studyEnvironmentConfigService;
        this.enrolleeService = enrolleeService;
        this.preEnrollmentResponseDao = preEnrollmentResponseDao;
        this.triggerService = triggerService;
        this.withdrawnEnrolleeDao = withdrawnEnrolleeDao;
        this.studyEnvironmentKitTypeService = studyEnvironmentKitTypeService;
        this.importService = importService;
        this.familyService = familyService;
        this.familyEnrolleeService = familyEnrolleeService;
        this.enrolleeRelationService = enrolleeRelationService;
        this.participantDataChangeService = participantDataChangeService;
        this.exportIntegrationService = exportIntegrationService;
        this.surveyService = surveyService;
    }

    public List<StudyEnvironment> findByStudy(UUID studyId) {
        return dao.findByStudy(studyId);
    }

    public Optional<StudyEnvironment> findByStudy(String studyShortcode, EnvironmentName environmentName) {
        return dao.findByStudy(studyShortcode, environmentName);
    }

    public List<StudyEnvironment> findAllByPortalAndEnvironment(UUID portalId, EnvironmentName environmentName) {
        return dao.findAllByPortalAndEnvironment(portalId, environmentName);
    }

    public StudyEnvironment verifyStudy(String studyShortcode, EnvironmentName environmentName) {
        return findByStudy(studyShortcode, environmentName).orElseThrow(() ->
                new NotFoundException("Study not found for environment %s: %s"
                        .formatted(environmentName, studyShortcode)));
    }

    @Transactional
    @Override
    public StudyEnvironment create(StudyEnvironment studyEnv) {
        StudyEnvironmentConfig envConfig = studyEnv.getStudyEnvironmentConfig();
        if (studyEnv.getStudyEnvironmentConfig() != null) {
            envConfig = studyEnvironmentConfigService.create(envConfig);
            studyEnv.setStudyEnvironmentConfigId(envConfig.getId());
        }
        StudyEnvironment newEnv = dao.create(studyEnv);
        for (StudyEnvironmentSurvey studyEnvironmentSurvey : studyEnv.getConfiguredSurveys()) {
            studyEnvironmentSurvey.setStudyEnvironmentId(newEnv.getId());
            studyEnvironmentSurveyDao.create(studyEnvironmentSurvey);
        }
        for (Trigger config : studyEnv.getTriggers()) {
            config.setStudyEnvironmentId(newEnv.getId());
            triggerService.create(config);
        }
        newEnv.setStudyEnvironmentConfig(envConfig);
        return newEnv;
    }

    @Transactional
    @Override
    public void delete(UUID studyEnvironmentId, Set<CascadeProperty> cascade) {
        StudyEnvironment studyEnv = dao.find(studyEnvironmentId).get();
        enrolleeRelationService.deleteByStudyEnvironmentId(studyEnvironmentId);
        familyEnrolleeService.deleteByStudyEnvironmentId(studyEnvironmentId);
        participantDataChangeService.deleteByStudyEnvironmentId(studyEnvironmentId);
        familyService.deleteByStudyEnvironmentId(studyEnvironmentId);
        enrolleeService.deleteByStudyEnvironmentId(studyEnv.getId(), cascade);
        studyEnvironmentSurveyDao.deleteByStudyEnvironmentId(studyEnvironmentId);
        triggerService.deleteByStudyEnvironmentId(studyEnvironmentId);
        preEnrollmentResponseDao.deleteByStudyEnvironmentId(studyEnvironmentId);
        withdrawnEnrolleeDao.deleteByStudyEnvironmentId(studyEnvironmentId);
        studyEnvironmentKitTypeService.deleteByStudyEnvironmentId(studyEnvironmentId, cascade);
        importService.deleteByStudyEnvId(studyEnvironmentId);
        exportIntegrationService.deleteByStudyEnvironmentId(studyEnvironmentId);
        dao.delete(studyEnvironmentId);
        if (studyEnv.getStudyEnvironmentConfigId() != null) {
            studyEnvironmentConfigService.delete(studyEnv.getStudyEnvironmentConfigId());
        }
    }

    @Transactional
    public void deleteByStudyId(UUID studyId, Set<CascadeProperty> cascade) {
        List<StudyEnvironment> studyEnvironments = dao.findByStudy(studyId);
        studyEnvironments.forEach(studyEnv -> {
            delete(studyEnv.getId(), cascade);
        });
    }

    public enum AllowedCascades implements CascadeProperty {
        ENVIRONMENT_CONFIG,
        ENROLLEE;
    }


    @Override
    public void loadForPublishing(StudyEnvironment studyEnv) {
        studyEnv.setConfiguredSurveys(studyEnvironmentSurveyDao.findAllWithSurvey(studyEnv.getId(), true));
        if (studyEnv.getPreEnrollSurveyId() != null) {
            studyEnv.setPreEnrollSurvey(surveyService.find(studyEnv.getPreEnrollSurveyId()).orElseThrow());
        }
    }

    @Override
    public void updateDiff(StudyEnvironmentChange change, StudyEnvironment sourceEnv, StudyEnvironment destEnv) {
        VersionedEntityChange<Survey> preEnrollChange = new VersionedEntityChange<Survey>(sourceEnv.getPreEnrollSurvey(), destEnv.getPreEnrollSurvey());
        ListChange<StudyEnvironmentSurvey, VersionedConfigChange<Survey>> surveyChanges = PortalEnvPublishable.diffConfigLists(
                sourceEnv.getConfiguredSurveys(),
                destEnv.getConfiguredSurveys(),
                getPublishIgnoreProps());
        change.setPreEnrollSurveyChanges(preEnrollChange);
        change.setSurveyChanges(surveyChanges);
    }

    @Override
    @Transactional
    public void applyDiff(StudyEnvironmentChange change, StudyEnvironment destEnv, PortalEnvironment destPortalEnv) {
        if (change.getPreEnrollSurveyChanges().isChanged()) {
            VersionedEntityChange<Survey> preEnrollChange = change.getPreEnrollSurveyChanges();
            UUID newSurveyId = null;
            if (preEnrollChange.newStableId() != null) {
                newSurveyId = surveyService.findByStableId(preEnrollChange.newStableId(), preEnrollChange.newVersion(), destPortalEnv.getPortalId()).get().getId();
            }
            destEnv.setPreEnrollSurveyId(newSurveyId);
            PublishingUtils.assignPublishedVersionIfNeeded(destEnv.getEnvironmentName(), destPortalEnv.getPortalId(), preEnrollChange, surveyService);
            update(destEnv);
        }

    }
}
