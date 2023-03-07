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
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StudyPopulator extends Populator<Study> {
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

    @Transactional
    @Override
    public Study populate(String filePathName) throws IOException {
        FilePopulateConfig config = new FilePopulateConfig(filePathName);
        return populate(config.getRootFileName(), config);
    }

    public Study populate(String studyFileName, FilePopulateConfig config) throws IOException {
        String portalFileString = filePopulateService.readFile(studyFileName, config);
        return populateFromString(portalFileString, config);
    }

    public Study populateFromString(String studyContent, FilePopulateConfig config)  throws IOException {
        StudyPopDto studyDto = objectMapper.readValue(studyContent, StudyPopDto.class);

        // delete the existing survey
        Optional<Study> existingStudy = studyService.findByShortcode(studyDto.getShortcode());
        existingStudy.ifPresent(study ->
            studyService.delete(study.getId(), new HashSet<>())
        );

        // first, populate the surveys and consent forms themselves
        for (String surveyFile : studyDto.getSurveyFiles()) {
            surveyPopulator.populate(config.newFrom(surveyFile));
        }
        for (String consentFile : studyDto.getConsentFormFiles()) {
            consentFormPopulator.populate(config.newFrom(consentFile));
        }
        for (String template : studyDto.getEmailTemplateFiles()) {
            emailTemplatePopulator.populate(config.newFrom(template));
        }
        for (StudyEnvironmentPopDto studyEnv : studyDto.getStudyEnvironmentDtos()) {
            initializeStudyEnvironmentDto(studyEnv, config.newForEnv(studyEnv.getEnvironmentName()));
        }

        Study newStudy = studyService.create(studyDto);

        for (StudyEnvironmentPopDto studyPopEnv : studyDto.getStudyEnvironmentDtos()) {
            postProcessStudyEnv(newStudy, studyPopEnv, config);
        }
        return newStudy;
    }

    /** takes a dto and hydrates it with already-populated objects (surveys, consents, etc...) */
    private void initializeStudyEnvironmentDto(StudyEnvironmentPopDto studyEnv, FilePopulateConfig config) {
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
            NotificationConfig notificationConfig = emailTemplatePopulator.convertNotificationConfig(configPopDto, config);
            studyEnv.getNotificationConfigs().add(notificationConfig);
        }
    }

    /** populates any objects that require an already-persisted study environment to save */
    private void postProcessStudyEnv(Study savedStudy, StudyEnvironmentPopDto studyPopEnv, FilePopulateConfig config)
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
            enrolleePopulator.populate(
                    config.newForStudy(enrolleeFile, savedStudy.getShortcode(), studyPopEnv.getEnvironmentName())
            );
        }
    }


}
