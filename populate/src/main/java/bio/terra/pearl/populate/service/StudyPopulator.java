package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.dao.kit.StudyEnvironmentKitTypeDao;
import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.core.model.kit.StudyEnvironmentKitType;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.publishing.StudyEnvironmentChange;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import bio.terra.pearl.core.service.publishing.PortalDiffService;
import bio.terra.pearl.core.service.publishing.StudyPublishingService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.populate.dto.export.ExportIntegrationPopDto;
import bio.terra.pearl.populate.dto.StudyEnvironmentPopDto;
import bio.terra.pearl.populate.dto.StudyPopDto;
import bio.terra.pearl.populate.dto.notifications.TriggerPopDto;
import bio.terra.pearl.populate.dto.survey.PreEnrollmentResponsePopDto;
import bio.terra.pearl.populate.dto.survey.StudyEnvironmentSurveyPopDto;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import bio.terra.pearl.populate.service.contexts.StudyPopulateContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class StudyPopulator extends BasePopulator<Study, StudyPopDto, PortalPopulateContext> {
    private final StudyService studyService;
    private final StudyEnvironmentService studyEnvService;
    private final EnrolleePopulator enrolleePopulator;
    private final FamilyPopulator familyPopulator;
    private final SurveyPopulator surveyPopulator;
    private final SurveyService surveyService;
    private final EmailTemplatePopulator emailTemplatePopulator;
    private final PreEnrollmentResponseDao preEnrollmentResponseDao;
    private final PortalDiffService portalDiffService;
    private final StudyPublishingService studyPublishingService;
    private final PortalEnvironmentService portalEnvironmentService;
    private final KitTypeDao kitTypeDao;
    private final StudyEnvironmentKitTypeDao studyEnvironmentKitTypeDao;
    private final ExportIntegrationPopulator exportIntegrationPopulator;

    public StudyPopulator(StudyService studyService,
                          StudyEnvironmentService studyEnvService, EnrolleePopulator enrolleePopulator, FamilyPopulator familyPopulator,
                          SurveyPopulator surveyPopulator, SurveyService surveyService,
                          EmailTemplatePopulator emailTemplatePopulator,
                          PreEnrollmentResponseDao preEnrollmentResponseDao,
                          PortalDiffService portalDiffService, StudyPublishingService studyPublishingService,
                          PortalEnvironmentService portalEnvironmentService, KitTypeDao kitTypeDao,
                          StudyEnvironmentKitTypeDao studyEnvironmentKitTypeDao,
                          ExportIntegrationPopulator exportIntegrationPopulator) {
        this.studyService = studyService;
        this.studyEnvService = studyEnvService;
        this.enrolleePopulator = enrolleePopulator;
        this.familyPopulator = familyPopulator;
        this.surveyPopulator = surveyPopulator;
        this.surveyService = surveyService;
        this.emailTemplatePopulator = emailTemplatePopulator;
        this.preEnrollmentResponseDao = preEnrollmentResponseDao;
        this.portalDiffService = portalDiffService;
        this.studyPublishingService = studyPublishingService;
        this.portalEnvironmentService = portalEnvironmentService;
        this.kitTypeDao = kitTypeDao;
        this.studyEnvironmentKitTypeDao = studyEnvironmentKitTypeDao;
        this.exportIntegrationPopulator = exportIntegrationPopulator;
    }

    /** takes a dto and hydrates it with already-populated objects (surveys, consents, etc...) */
    private void initializeStudyEnvironmentDto(StudyEnvironmentPopDto studyEnv, PortalPopulateContext context) {
        for (int i = 0; i < studyEnv.getConfiguredSurveyDtos().size(); i++) {
            StudyEnvironmentSurveyPopDto configSurveyDto = studyEnv.getConfiguredSurveyDtos().get(i);
            StudyEnvironmentSurvey configSurvey = surveyPopulator.convertConfiguredSurvey(configSurveyDto, i, context, context.getPortalShortcode());
            studyEnv.getConfiguredSurveys().add(configSurvey);
        }
        if (studyEnv.getPreEnrollSurveyDto() != null) {
            Survey preEnrollSurvey = surveyPopulator.findFromDto(studyEnv.getPreEnrollSurveyDto(), context).get();
            studyEnv.setPreEnrollSurveyId(preEnrollSurvey.getId());
            studyEnv.setPreEnrollSurvey(preEnrollSurvey);
        }
        for (TriggerPopDto configPopDto : studyEnv.getTriggerDtos()) {
            Trigger trigger = emailTemplatePopulator.convertTrigger(configPopDto, context);
            studyEnv.getTriggers().add(trigger);
        }
    }

    /** populates any objects that require an already-persisted study environment to save */
    private void postProcessStudyEnv(StudyEnvironmentPopDto studyPopEnv, StudyPopulateContext context, boolean overwrite)
    throws IOException {
        StudyEnvironment savedEnv = studyEnvService.findByStudy(context.getStudyShortcode(), studyPopEnv.getEnvironmentName()).get();

        // save any of the pre-enrollment responses that aren't associated with an enrollee
        for (PreEnrollmentResponsePopDto responsePopDto : studyPopEnv.getPreEnrollmentResponseDtos()) {
            Survey survey = surveyService.findByStableIdAndPortalShortcode(responsePopDto.getSurveyStableId(),
                    responsePopDto.getSurveyVersion(), context.getPortalShortcode()).get();
            String fullData = objectMapper.writeValueAsString(responsePopDto.getAnswers());
            PreEnrollmentResponse response = PreEnrollmentResponse.builder()
                    .surveyId(survey.getId())
                    .studyEnvironmentId(savedEnv.getId())
                    .qualified(responsePopDto.isQualified())
                    .fullData(fullData)
                    .build();
            preEnrollmentResponseDao.create(response);
        }
        for (String kitTypeName : studyPopEnv.getKitTypeNames()) {
            KitType kitType = kitTypeDao.findByName(kitTypeName).get();
            studyEnvironmentKitTypeDao.create(StudyEnvironmentKitType.builder()
                    .studyEnvironmentId(savedEnv.getId())
                    .kitTypeId(kitType.getId())
                    .build());
        }
        // now populate enrollees
        for (String enrolleeFile : studyPopEnv.getEnrolleeFiles()) {
            enrolleePopulator.populate(context.newFrom(enrolleeFile), overwrite);
        }

        for (String familyFile : studyPopEnv.getFamilyFiles()) {
            familyPopulator.populate(context.newFrom(familyFile), overwrite);
        }

        for (ExportIntegrationPopDto exportIntegration : studyPopEnv.getExportIntegrationPopDtos()) {
            exportIntegrationPopulator.populateFromDto(exportIntegration, context, overwrite);
        }
    }


    @Override
    protected Class<StudyPopDto> getDtoClazz() {
        return StudyPopDto.class;
    }

    @Override
    public void preProcessDto(StudyPopDto popDto, PortalPopulateContext context) {
        popDto.setShortcode(context.applyShortcodeOverride(popDto.getShortcode()));
        popDto.setName(context.applyShortcodeOverride(popDto.getName()));
    }

    @Override
    public Optional<Study> findFromDto(StudyPopDto popDto, PortalPopulateContext context) {
        return studyService.findByShortcode(popDto.getShortcode());
    }

    @Override
    public Study overwriteExisting(Study existingObj, StudyPopDto popDto, PortalPopulateContext context) throws IOException {
        studyService.delete(existingObj.getId(), CascadeProperty.EMPTY_SET);
        return createNew(popDto, context, true);
    }

    @Override
    public Study createPreserveExisting(Study existingObj, StudyPopDto popDto, PortalPopulateContext context) throws IOException {
        existingObj.setName(popDto.getName());
        Study study = studyService.update(existingObj);
        return createOrUpdate(study, popDto, context, false);
    }

    @Override
    public Study createNew(StudyPopDto popDto, PortalPopulateContext context, boolean overwrite) throws IOException {
        return createOrUpdate(null, popDto, context, overwrite);
    }

    protected Study createOrUpdate(Study existingStudy, StudyPopDto popDto, PortalPopulateContext context, boolean overwrite) throws IOException {
        // first, populate the surveys and consent forms themselves
        populateDocuments(popDto, context, overwrite);
        for (StudyEnvironmentPopDto studyEnv : popDto.getStudyEnvironmentDtos()) {
            initializeStudyEnvironmentDto(studyEnv, context.newFrom(studyEnv.getEnvironmentName()));
        }
        if (existingStudy == null) {
            existingStudy = studyService.create(popDto);
        }

        if (!overwrite) {
            // if we're not overwriting, we just want to update the sandbox configuration
            StudyEnvironment sourceEnv = popDto.getStudyEnvironmentDtos().stream()
                    .filter(env -> env.getEnvironmentName().equals(EnvironmentName.sandbox))
                    .findFirst().get();
            StudyEnvironment destEnv = portalDiffService.
                    loadStudyEnvForProcessing(existingStudy.getShortcode(), EnvironmentName.sandbox);
            PortalEnvironment destPortalEnv = portalEnvironmentService
                    .findOne(context.getPortalShortcode(), EnvironmentName.sandbox).get();
            try {
                StudyEnvironmentChange studyEnvChange = portalDiffService.diffStudyEnvs(existingStudy.getShortcode(),
                        sourceEnv, destEnv);
                studyPublishingService.applyChanges(destEnv, studyEnvChange, destPortalEnv);
            } catch (Exception e) {
                // we probably want to move this to some sort of "PopulateException"
                throw new IOException(e);
            }

        }


        StudyPopulateContext studyEnvContext = new StudyPopulateContext(context, existingStudy.getShortcode());
        for (StudyEnvironmentPopDto studyPopEnv : popDto.getStudyEnvironmentDtos()) {
            postProcessStudyEnv(studyPopEnv, studyEnvContext.newFrom(studyPopEnv.getEnvironmentName()), overwrite);
        }
        return existingStudy;
    }

    protected void populateDocuments(StudyPopDto popDto, PortalPopulateContext context, boolean overwrite) throws IOException {
        for (String surveyFile : popDto.getSurveyFiles()) {
            surveyPopulator.populate(context.newFrom(surveyFile), overwrite);
        }
        for (String template : popDto.getEmailTemplateFiles()) {
            emailTemplatePopulator.populate(context.newFrom(template), overwrite);
        }
    }
}
