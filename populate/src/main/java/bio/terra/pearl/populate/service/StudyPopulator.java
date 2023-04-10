package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.populate.dto.StudyEnvironmentPopDto;
import bio.terra.pearl.populate.dto.StudyPopDto;
import bio.terra.pearl.populate.dto.consent.StudyEnvironmentConsentPopDto;
import bio.terra.pearl.populate.dto.notifications.NotificationConfigPopDto;
import bio.terra.pearl.populate.dto.survey.PreEnrollmentResponsePopDto;
import bio.terra.pearl.populate.dto.survey.StudyEnvironmentSurveyPopDto;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import bio.terra.pearl.populate.service.contexts.StudyPopulateContext;
import java.io.IOException;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class StudyPopulator extends Populator<Study, StudyPopDto, PortalPopulateContext> {
    private StudyService studyService;
    private EnrolleePopulator enrolleePopulator;
    private SurveyPopulator surveyPopulator;
    private SurveyService surveyService;
    private ConsentFormPopulator consentFormPopulator;
    private EmailTemplatePopulator emailTemplatePopulator;
    private PreEnrollmentResponseDao preEnrollmentResponseDao;

    public StudyPopulator(StudyService studyService,
                          EnrolleePopulator enrolleePopulator,
                          SurveyPopulator surveyPopulator, SurveyService surveyService,
                          ConsentFormPopulator consentFormPopulator,
                          EmailTemplatePopulator emailTemplatePopulator,
                          PreEnrollmentResponseDao preEnrollmentResponseDao) {
        this.studyService = studyService;
        this.enrolleePopulator = enrolleePopulator;
        this.surveyPopulator = surveyPopulator;
        this.surveyService = surveyService;
        this.consentFormPopulator = consentFormPopulator;
        this.emailTemplatePopulator = emailTemplatePopulator;
        this.preEnrollmentResponseDao = preEnrollmentResponseDao;
    }

    /** takes a dto and hydrates it with already-populated objects (surveys, consents, etc...) */
    private void initializeStudyEnvironmentDto(StudyEnvironmentPopDto studyEnv, PortalPopulateContext context) {
        for (int i = 0; i < studyEnv.getConfiguredSurveyDtos().size(); i++) {
            StudyEnvironmentSurveyPopDto configSurveyDto = studyEnv.getConfiguredSurveyDtos().get(i);
            StudyEnvironmentSurvey configSurvey = surveyPopulator.convertConfiguredSurvey(configSurveyDto, i);
            studyEnv.getConfiguredSurveys().add(configSurvey);
        }
        if (studyEnv.getPreEnrollSurveyDto() != null) {
            Survey preEnrollSurvey = surveyPopulator.findFromDto(studyEnv.getPreEnrollSurveyDto(), context).get();
            studyEnv.setPreEnrollSurveyId(preEnrollSurvey.getId());
        }
        for (int i = 0; i < studyEnv.getConfiguredConsentDtos().size(); i++) {
            StudyEnvironmentConsentPopDto configConsentDto = studyEnv.getConfiguredConsentDtos().get(i);
            StudyEnvironmentConsent configConsent = consentFormPopulator.convertConfiguredConsent(configConsentDto, i);
            studyEnv.getConfiguredConsents().add(configConsent);
        }
        for (NotificationConfigPopDto configPopDto : studyEnv.getNotificationConfigDtos()) {
            NotificationConfig notificationConfig = emailTemplatePopulator.convertNotificationConfig(configPopDto, context);
            studyEnv.getNotificationConfigs().add(notificationConfig);
        }
    }

    /** populates any objects that require an already-persisted study environment to save */
    private void postProcessStudyEnv(Study savedStudy, StudyEnvironmentPopDto studyPopEnv, StudyPopulateContext context, boolean overwrite)
    throws IOException {
        StudyEnvironment savedEnv = savedStudy.getStudyEnvironments().stream().filter(env ->
                env.getEnvironmentName().equals(studyPopEnv.getEnvironmentName())).findFirst().get();
        // save any of the pre-enrollment responses that aren't associated with an enrollee
        for (PreEnrollmentResponsePopDto responsePopDto : studyPopEnv.getPreEnrollmentResponseDtos()) {
            Survey survey = surveyService.findByStableId(responsePopDto.getSurveyStableId(),
                    responsePopDto.getSurveyVersion()).get();
            PreEnrollmentResponse response = PreEnrollmentResponse.builder()
                    .surveyId(survey.getId())
                    .studyEnvironmentId(savedEnv.getId())
                    .qualified(responsePopDto.isQualified())
                    .fullData(responsePopDto.getFullDataJson().toString())
                    .build();
            preEnrollmentResponseDao.create(response);
        }
        // now populate enrollees
        for (String enrolleeFile : studyPopEnv.getEnrolleeFiles()) {
            enrolleePopulator.populate(context.newFrom(enrolleeFile), overwrite);
        }
    }


    @Override
    protected Class<StudyPopDto> getDtoClazz() {
        return StudyPopDto.class;
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
        return populateChildren(study, popDto, context, true);
    }

    @Override
    public Study createNew(StudyPopDto popDto, PortalPopulateContext context, boolean overwrite) throws IOException {
        Study study = studyService.create(popDto);
        return populateChildren(study, popDto, context, overwrite);
    }

    protected Study populateChildren(Study study, StudyPopDto popDto, PortalPopulateContext context, boolean overwrite) throws IOException {
        // first, populate the surveys and consent forms themselves
        for (String surveyFile : popDto.getSurveyFiles()) {
            surveyPopulator.populate(context.newFrom(surveyFile), overwrite);
        }
        for (String consentFile : popDto.getConsentFormFiles()) {
            consentFormPopulator.populate(context.newFrom(consentFile), overwrite);
        }
        for (String template : popDto.getEmailTemplateFiles()) {
            emailTemplatePopulator.populate(context.newFrom(template), overwrite);
        }
        for (StudyEnvironmentPopDto studyEnv : popDto.getStudyEnvironmentDtos()) {
            initializeStudyEnvironmentDto(studyEnv, context.newFrom(studyEnv.getEnvironmentName()));
        }

        Study newStudy = studyService.create(popDto);
        StudyPopulateContext studyEnvContext = new StudyPopulateContext(context, newStudy.getShortcode());
        for (StudyEnvironmentPopDto studyPopEnv : popDto.getStudyEnvironmentDtos()) {
            postProcessStudyEnv(newStudy, studyPopEnv, studyEnvContext.newFrom(studyPopEnv.getEnvironmentName()), overwrite);
        }
        return newStudy;
    }
}
