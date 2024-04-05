package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.dao.survey.AnswerMappingDao;
import bio.terra.pearl.core.dao.survey.SurveyQuestionDefinitionDao;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.populate.dao.SurveyPopulateDao;
import bio.terra.pearl.populate.dto.survey.StudyEnvironmentSurveyPopDto;
import bio.terra.pearl.populate.dto.survey.SurveyPopDto;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** populator for surveys and consent forms */
@Service
public class SurveyPopulator extends BasePopulator<Survey, SurveyPopDto, PortalPopulateContext> {
    private SurveyService surveyService;
    private PortalService portalService;
    private SurveyPopulateDao surveyPopulateDao;
    private AnswerMappingDao answerMappingDao;
    private SurveyQuestionDefinitionDao surveyQuestionDefinitionDao;

    public SurveyPopulator(SurveyService surveyService,
                           PortalService portalService,
                           SurveyPopulateDao surveyPopulateDao,
                           SurveyQuestionDefinitionDao surveyQuestionDefinitionDao,
                           AnswerMappingDao answerMappingDao) {
        this.portalService = portalService;
        this.surveyPopulateDao = surveyPopulateDao;
        this.surveyService = surveyService;
        this.answerMappingDao = answerMappingDao;
        this.surveyQuestionDefinitionDao = surveyQuestionDefinitionDao;
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


}
