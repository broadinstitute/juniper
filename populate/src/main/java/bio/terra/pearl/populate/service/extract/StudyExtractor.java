package bio.terra.pearl.populate.service.extract;

import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.notification.NotificationConfig;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.study.PortalStudy;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.service.study.*;
import bio.terra.pearl.core.service.study.exception.StudyEnvConfigMissing;
import bio.terra.pearl.populate.dto.StudyEnvironmentPopDto;
import bio.terra.pearl.populate.dto.StudyPopDto;
import bio.terra.pearl.populate.dto.consent.StudyEnvironmentConsentPopDto;
import bio.terra.pearl.populate.dto.survey.StudyEnvironmentSurveyPopDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudyExtractor {
    private final ObjectMapper objectMapper;
    private final StudyService studyService;
    private final PortalStudyService portalStudyService;
    private final StudyEnvironmentService studyEnvironmentService;
    private final StudyEnvironmentConfigService studyEnvironmentConfigService;
    private final StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private final StudyEnvironmentConsentService studyEnvironmentConsentService;

    public StudyExtractor(@Qualifier("extractionObjectMapper") ObjectMapper objectMapper, StudyService studyService,
                          PortalStudyService portalStudyService, StudyEnvironmentService studyEnvironmentService,
                          StudyEnvironmentConfigService studyEnvironmentConfigService, StudyEnvironmentSurveyService studyEnvironmentSurveyService, StudyEnvironmentConsentService studyEnvironmentConsentService) {
        this.studyService = studyService;
        this.portalStudyService = portalStudyService;
        this.studyEnvironmentService = studyEnvironmentService;
        this.studyEnvironmentConfigService = studyEnvironmentConfigService;
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.objectMapper = objectMapper;
        this.studyEnvironmentConsentService = studyEnvironmentConsentService;
        objectMapper.addMixIn(Study.class, StudyMixin.class);
        objectMapper.addMixIn(StudyEnvironment.class, StudyEnvironmentMixin.class);
    }

    public void writeStudies(Portal portal, ExtractPopulateContext context) {
        List<PortalStudy> portalStudyList = portalStudyService.findByPortalId(portal.getId());
        for (PortalStudy portalStudy : portalStudyList) {
            writeStudy(portalStudy, context);
        }
    }

    public void writeStudy(PortalStudy portalStudy, ExtractPopulateContext context) {
        Study study = studyService.find(portalStudy.getStudyId()).orElseThrow();
        String studyFileName = "studies/%s/study.json".formatted(study.getShortcode());
        StudyPopDto studyPopDto = new StudyPopDto();
        studyPopDto.setShortcode(study.getShortcode());
        studyPopDto.setName(study.getName());
        List<StudyEnvironment> studyEnvs = studyEnvironmentService.findByStudy(study.getId());
        for (StudyEnvironment studyEnv : studyEnvs) {
            StudyEnvironmentPopDto studyEnvPopDto = extractStudyEnv(studyEnv, studyPopDto, context);
            studyPopDto.getStudyEnvironmentDtos().add(studyEnvPopDto);
        }
        try {
            String studyString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(studyPopDto);
            context.writeFileForEntity(studyFileName, studyString, study.getId());
            context.getPortalPopDto().getPopulateStudyFiles().add(studyFileName);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error writing study %s to json".formatted(study.getShortcode()), e);
        }
    }

    public StudyEnvironmentPopDto extractStudyEnv(StudyEnvironment studyEnv,
                                                  StudyPopDto studyPopDto,
                                                  ExtractPopulateContext context) {
        StudyEnvironmentPopDto studyEnvPopDto = new StudyEnvironmentPopDto();
        studyEnvPopDto.setEnvironmentName(studyEnv.getEnvironmentName());
        studyEnvPopDto.setStudyEnvironmentConfig(
                studyEnvironmentConfigService.find(studyEnv.getStudyEnvironmentConfigId()).orElseThrow(StudyEnvConfigMissing::new)
        );
        if (studyEnv.getPreEnrollSurveyId() != null) {
            SurveyExtractor.SurveyPopDtoStub surveyPopDtoStub = new SurveyExtractor.SurveyPopDtoStub();
            surveyPopDtoStub.setPopulateFileName("../../" + context.getFileNameForEntity(studyEnv.getPreEnrollSurveyId()));
            studyEnvPopDto.setPreEnrollSurveyDto(surveyPopDtoStub);
        }
        List<StudyEnvironmentSurvey> studyEnvSurveys = studyEnvironmentSurveyService.findAllByStudyEnvId(studyEnv.getId());
        for (StudyEnvironmentSurvey studyEnvSurvey : studyEnvSurveys) {;
            StudyEnvironmentSurveyPopDto studyEnvSurveyPopDto = new StudyEnvironmentSurveyPopDto();
            BeanUtils.copyProperties(studyEnvSurvey, studyEnvSurveyPopDto, "id", "studyEnvironmentId", "surveyId");
            studyEnvSurveyPopDto.setPopulateFileName("../../" + context.getFileNameForEntity(studyEnvSurvey.getSurveyId()));
            studyEnvPopDto.getConfiguredSurveyDtos().add(studyEnvSurveyPopDto);
        }
        List<StudyEnvironmentConsent> studyEnvConsents = studyEnvironmentConsentService.findAllByStudyEnvironmentId(studyEnv.getId());
        for (StudyEnvironmentConsent studyEnvConsent : studyEnvConsents) {;
            StudyEnvironmentConsentPopDto consentPopDto = new StudyEnvironmentConsentPopDto();
            BeanUtils.copyProperties(studyEnvConsent, consentPopDto, "id", "studyEnvironmentId", "consentFormId");
            String filename = "../../" + context.getFileNameForEntity(studyEnvConsent.getConsentFormId());
            consentPopDto.setPopulateFileName(filename);
            studyEnvPopDto.getConfiguredConsentDtos().add(consentPopDto);
        }
        return studyEnvPopDto;
    }



    protected static class StudyMixin {
        @JsonIgnore
        public List<StudyEnvironment> getStudyEnvironments() { return null; }
    }
    protected static class StudyEnvironmentMixin {
        @JsonIgnore
        public List<StudyEnvironmentSurvey> getStudyEnvironmentSurveys() { return null; }
        @JsonIgnore
        public List<NotificationConfig> getNotificationConfigs() { return null; }
        @JsonIgnore
        public List<StudyEnvironmentConsent> getConfiguredConsents() { return null; }
        @JsonIgnore
        public List<StudyEnvironmentSurvey> getConfiguredSurveys() { return null; }
    }
}
