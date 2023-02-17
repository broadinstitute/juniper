package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.populate.dto.StudyEnvironmentPopDto;
import bio.terra.pearl.populate.dto.StudyPopDto;
import bio.terra.pearl.populate.dto.consent.StudyEnvironmentConsentPopDto;
import bio.terra.pearl.populate.dto.notifications.NotificationConfigPopDto;
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
    private ConsentFormPopulator consentFormPopulator;
    private EmailTemplatePopulator emailTemplatePopulator;

    public StudyPopulator(StudyService studyService,
                          EnrolleePopulator enrolleePopulator,
                          SurveyPopulator surveyPopulator, ConsentFormPopulator consentFormPopulator,
                          EmailTemplatePopulator emailTemplatePopulator) {
        this.studyService = studyService;
        this.enrolleePopulator = enrolleePopulator;
        this.surveyPopulator = surveyPopulator;
        this.consentFormPopulator = consentFormPopulator;
        this.emailTemplatePopulator = emailTemplatePopulator;
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

        for (StudyEnvironmentPopDto studyEnv : studyDto.getStudyEnvironmentDtos()) {
            for (String enrolleeFile : studyEnv.getEnrolleeFiles()) {
                enrolleePopulator.populate(
                        config.newForStudy(enrolleeFile, newStudy.getShortcode(), studyEnv.getEnvironmentName())
                );
            }
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


}
