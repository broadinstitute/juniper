package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.dao.survey.AnswerMappingDao;
import bio.terra.pearl.core.dao.survey.SurveyQuestionDefinitionDao;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.consent.ConsentFormService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConsentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.populate.dao.SurveyPopulateDao;
import bio.terra.pearl.populate.dto.survey.StudyEnvironmentSurveyPopDto;
import bio.terra.pearl.populate.dto.survey.SurveyPopDto;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;

/** populator for surveys and consent forms */
@Service
@Slf4j
public class SurveyPopulator extends BasePopulator<Survey, SurveyPopDto, PortalPopulateContext> {
    private final SurveyService surveyService;
    private final PortalService portalService;
    private final SurveyPopulateDao surveyPopulateDao;
    private final AnswerMappingDao answerMappingDao;
    private final SurveyQuestionDefinitionDao surveyQuestionDefinitionDao;
    private final StudyEnvironmentConsentService studyEnvConsentService;
    private final StudyEnvironmentSurveyService studyEnvironmentSurveyService;
    private final ConsentFormService consentFormService;

    public SurveyPopulator(SurveyService surveyService,
                           PortalService portalService,
                           SurveyPopulateDao surveyPopulateDao,
                           SurveyQuestionDefinitionDao surveyQuestionDefinitionDao,
                           AnswerMappingDao answerMappingDao,
                           StudyEnvironmentConsentService studyEnvConsentService,
                           StudyEnvironmentSurveyService studyEnvironmentSurveyService, ConsentFormService consentFormService) {
        this.portalService = portalService;
        this.surveyPopulateDao = surveyPopulateDao;
        this.surveyService = surveyService;
        this.answerMappingDao = answerMappingDao;
        this.surveyQuestionDefinitionDao = surveyQuestionDefinitionDao;
        this.studyEnvConsentService = studyEnvConsentService;
        this.studyEnvironmentSurveyService = studyEnvironmentSurveyService;
        this.consentFormService = consentFormService;
    }

    @Override
    protected void preProcessDto(SurveyPopDto popDto, PortalPopulateContext context) {
        UUID portalId = portalService.findOneByShortcode(context.getPortalShortcode()).get().getId();
        popDto.setPortalId(portalId);
        popDto.setStableId(context.applyShortcodeOverride(popDto.getStableId()));
        String newContent = popDto.getJsonContent().toString();
        popDto.setContent(newContent);
    }

    public StudyEnvironmentSurvey convertConfiguredSurvey(StudyEnvironmentSurveyPopDto configuredSurveyDto,
                                                          int index, FilePopulateContext context, String portalShortcode) {
        StudyEnvironmentSurvey configuredSurvey = new StudyEnvironmentSurvey();
        BeanUtils.copyProperties(configuredSurveyDto, configuredSurvey);
        Survey survey;
        if (configuredSurveyDto.getPopulateFileName() != null) {
            survey = context.fetchFromPopDto(configuredSurveyDto, surveyService).get();
        } else {
            survey = surveyService.findByStableIdAndPortalShortcode(configuredSurveyDto.getSurveyStableId(),
                    configuredSurveyDto.getSurveyVersion(), portalShortcode).get();
        }
        configuredSurvey.setSurveyId(survey.getId());
        configuredSurvey.setSurvey(survey);
        configuredSurvey.setSurveyOrder(index);
        return configuredSurvey;
    }

    @Override
    protected Class<SurveyPopDto> getDtoClazz() {
        return SurveyPopDto.class;
    }

    @Override
    public Optional<Survey> findFromDto(SurveyPopDto popDto, PortalPopulateContext context) {
        Optional<Survey> existingOpt = context.fetchFromPopDto(popDto, surveyService);
        if (existingOpt.isPresent()) {
            return existingOpt;
        }
        // load with mappings since we'll check those for equality
        return surveyService.findByStableIdAndPortalShortcodeWithMappings(popDto.getStableId(), popDto.getVersion(), context.getPortalShortcode());
    }

    @Override
    public Survey overwriteExisting(Survey existingObj, SurveyPopDto popDto, PortalPopulateContext context) throws IOException {
        BeanUtils.copyProperties(popDto, existingObj, "id", "stableId", "version", "createdAt", "lastUpdatedAt", "answerMappings", "portalId");
        surveyPopulateDao.update(existingObj);
        updateAnswerMappings(existingObj, popDto);
        surveyQuestionDefinitionDao.deleteBySurveyId(existingObj.getId());
        popDto.setId(existingObj.getId());
        for (SurveyQuestionDefinition questionDefinition : surveyService.getSurveyQuestionDefinitions(popDto)) {
            surveyQuestionDefinitionDao.create(questionDefinition);
        }
        return existingObj;
    }


    private void updateAnswerMappings(Survey existingSurvey, SurveyPopDto surveyPopDto) {
        answerMappingDao.deleteBySurveyId(existingSurvey.getId());
        existingSurvey.getAnswerMappings().clear();
        for (AnswerMapping answerMapping : surveyPopDto.getAnswerMappings()) {
            answerMapping.setSurveyId(existingSurvey.getId());
            existingSurvey.getAnswerMappings().add(answerMappingDao.create(answerMapping));
        }
    }

    @Override
    public Survey createPreserveExisting(Survey existingObj, SurveyPopDto popDto, PortalPopulateContext context) throws IOException {
        if (Objects.equals(existingObj.getContent(), popDto.getContent()) &&
                isAnswerMappingsEqual(existingObj.getAnswerMappings(), popDto.getAnswerMappings())) {
            // the things are the same, don't bother creating a new version
            return existingObj;
        }
        int newVersion = surveyService.getNextVersionByPortalShortcode(popDto.getStableId(), context.getPortalShortcode());
        popDto.setVersion(newVersion);
        return createNew(popDto, context, false);
    }

    @Override
    public Survey createNew(SurveyPopDto popDto, PortalPopulateContext context, boolean overwrite) throws IOException {
        return surveyService.create(popDto);
    }

    public boolean isAnswerMappingsEqual(List<AnswerMapping> mappingsA, List<AnswerMapping> mappingsB) {
        if (mappingsA == null || mappingsB == null) {
            return mappingsA == mappingsB;
        }
        if (mappingsA.size() != mappingsB.size()) {
            return false;
        }
        for (int i = 0; i < mappingsA.size(); i++) {
            if (!isAnswerMappingEqual(mappingsA.get(i), mappingsB.get(i))) {
                return false;
            }
        }
        return true;
    }

    public boolean isAnswerMappingEqual(AnswerMapping mapA, AnswerMapping mapB) {
        return Objects.equals(mapA.getMapType(), mapB.getMapType()) &&
                Objects.equals(mapA.getTargetType(), mapB.getTargetType()) &&
                Objects.equals(mapA.getFormatString(), mapB.getFormatString()) &&
                Objects.equals(mapA.getTargetField(), mapB.getTargetField()) &&
                Objects.equals(mapA.getQuestionStableId(), mapB.getQuestionStableId()) &&
                Objects.equals(mapA.isErrorOnFail(), mapB.isErrorOnFail());
    }

    @Transactional
    /** migration method to convert any existing ConsentForm objects to Surveys */
    public Map<String, Object> convertAllConsentForms() {
        List<Portal> portals = portalService.findAll();
        int formsConverted = 0;
        List<String> formsErrored = new ArrayList<>();
        for (Portal portal : portals) {
            List<ConsentForm> allConsents = consentFormService.findByPortalId(portal.getId());
            for (ConsentForm form : allConsents) {
                try {
                    convertConsentForm(form);
                } catch (Exception e) {
                    log.warn("Error converting consent %s v%d".formatted(form.getStableId(), form.getVersion()), e);
                    formsErrored.add("%s v%d".formatted(form.getStableId(), form.getVersion()));
                }
            }
            formsConverted += allConsents.size();
        }
        return Map.of("formsConverted", formsConverted, "formsErrored", formsErrored);
    }

    public Survey convertConsentForm(ConsentForm form) {
        Survey survey = Survey.builder()
                .name(form.getName())
                .stableId(form.getStableId())
                .version(form.getVersion())
                .content(form.getContent())
                .surveyType(SurveyType.CONSENT)
                .required(true)
                .portalId(form.getPortalId())
                .allowAdminEdit(false)
                .allowParticipantReedit(false)
                .build();
        survey = surveyService.create(survey);
        List<StudyEnvironmentConsent> studyEnvConsents = studyEnvConsentService.findAllByConsentForm(form.getId());
        for (StudyEnvironmentConsent studyEnvConsent : studyEnvConsents) {
            convertStudyEnvConsent(studyEnvConsent, survey);
        }
        return survey;
    }

    /** creates a studyEnvironmentSurvey based on the studyEnvConsent, then deletes the StudyEnvConsent */
    public StudyEnvironmentSurvey convertStudyEnvConsent(StudyEnvironmentConsent studyEnvConsent, Survey survey) {
        StudyEnvironmentSurvey studyEnvironmentSurvey = StudyEnvironmentSurvey.builder()
                .studyEnvironmentId(studyEnvConsent.getStudyEnvironmentId())
                .surveyId(survey.getId())
                .active(true) // study environment consents were all active
                .surveyOrder(studyEnvConsent.getConsentOrder()).build();
        studyEnvConsentService.delete(studyEnvConsent.getId(), CascadeProperty.EMPTY_SET);
        return studyEnvironmentSurveyService.create(studyEnvironmentSurvey);

    }

}
