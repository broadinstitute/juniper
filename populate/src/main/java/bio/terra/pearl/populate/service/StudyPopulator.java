package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.dao.survey.PreEnrollmentResponseDao;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
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
import java.util.HashSet;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class StudyPopulator extends Populator<Study, PortalPopulateContext> {
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

    public Study populateFromString(String studyContent, PortalPopulateContext context)  throws IOException {
        StudyPopDto studyDto = objectMapper.readValue(studyContent, StudyPopDto.class);

        // delete the existing survey
        Optional<Study> existingStudy = studyService.findByShortcode(studyDto.getShortcode());
        existingStudy.ifPresent(study ->
            studyService.delete(study.getId(), new HashSet<>())
        );

        // first, populate the surveys and consent forms themselves
        for (String surveyFile : studyDto.getSurveyFiles()) {
            surveyPopulator.populate(context.newFrom(surveyFile));
        }
        for (String consentFile : studyDto.getConsentFormFiles()) {
            consentFormPopulator.populate(context.newFrom(consentFile));
        }
        for (String template : studyDto.getEmailTemplateFiles()) {
            emailTemplatePopulator.populate(context.newFrom(template));
        }
        for (StudyEnvironmentPopDto studyEnv : studyDto.getStudyEnvironmentDtos()) {
            initializeStudyEnvironmentDto(studyEnv, context.newFrom(studyEnv.getEnvironmentName()));
        }

        Study newStudy = studyService.create(studyDto);
        StudyPopulateContext studyEnvContext = new StudyPopulateContext(context, newStudy.getShortcode());
        for (StudyEnvironmentPopDto studyPopEnv : studyDto.getStudyEnvironmentDtos()) {
            postProcessStudyEnv(newStudy, studyPopEnv, studyEnvContext.newFrom(studyPopEnv.getEnvironmentName()));
        }
        return newStudy;
    }

    /** takes a dto and hydrates it with already-populated objects (surveys, consents, etc...) */
    private void initializeStudyEnvironmentDto(StudyEnvironmentPopDto studyEnv, PortalPopulateContext context) {
        for (int i = 0; i < studyEnv.getConfiguredSurveyDtos().size(); i++) {
            StudyEnvironmentSurveyPopDto configSurveyDto = studyEnv.getConfiguredSurveyDtos().get(i);
            StudyEnvironmentSurvey configSurvey = surveyPopulator.convertConfiguredSurvey(configSurveyDto, i);
            studyEnv.getConfiguredSurveys().add(configSurvey);
        }
        if (studyEnv.getPreEnrollSurveyDto() != null) {
            Survey preEnrollSurvey = surveyPopulator.fetchFromPopDto(studyEnv.getPreEnrollSurveyDto()).get();
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
    private void postProcessStudyEnv(Study savedStudy, StudyEnvironmentPopDto studyPopEnv, StudyPopulateContext context)
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
            enrolleePopulator.populate(context.newFrom(enrolleeFile));
        }
    }


}
