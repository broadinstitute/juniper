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
import bio.terra.pearl.populate.service.contexts.PortalPopulateContext;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/** populator for surveys and consent forms */
@Service
public class SurveyPopulator extends Populator<Survey, SurveyPopDto, PortalPopulateContext> {
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
    protected void updateDtoFromContext(SurveyPopDto popDto, PortalPopulateContext context) {
        UUID portalId = portalService.findOneByShortcode(context.getPortalShortcode()).get().getId();
        popDto.setPortalId(portalId);
        String newContent = popDto.getJsonContent().toString();
        popDto.setContent(newContent);
    }

    private void updateAnswerMappings(Survey existingSurvey, SurveyPopDto surveyPopDto) {
        answerMappingDao.deleteBySurveyId(existingSurvey.getId());
        existingSurvey.getAnswerMappings().clear();
        for (AnswerMapping answerMapping : surveyPopDto.getAnswerMappings()) {
            answerMapping.setSurveyId(existingSurvey.getId());
            existingSurvey.getAnswerMappings().add(answerMappingDao.create(answerMapping));
        }
    }

    public StudyEnvironmentSurvey convertConfiguredSurvey(StudyEnvironmentSurveyPopDto configuredSurveyDto, int index) {
        StudyEnvironmentSurvey configuredSurvey = new StudyEnvironmentSurvey();
        BeanUtils.copyProperties(configuredSurveyDto, configuredSurvey);
        Survey survey = surveyService.findByStableId(configuredSurveyDto.getSurveyStableId(),
                configuredSurveyDto.getSurveyVersion()).get();
        configuredSurvey.setSurveyId(survey.getId());
        configuredSurvey.setSurveyOrder(index);
        return configuredSurvey;
    }

    @Override
    protected Class<SurveyPopDto> getDtoClazz() {
        return SurveyPopDto.class;
    }

    @Override
    public Optional<Survey> findFromDto(SurveyPopDto popDto, PortalPopulateContext context) {
        if (popDto.getPopulateFileName() != null) {
            return context.fetchFromPopDto(popDto, surveyService);
        }
        return surveyService.findByStableId(popDto.getStableId(), popDto.getVersion());
    }

    @Override
    public Survey overwriteExisting(Survey existingObj, SurveyPopDto popDto, PortalPopulateContext context) throws IOException {
        existingObj.setContent(popDto.getContent());
        existingObj.setName(popDto.getName());
        surveyPopulateDao.update(existingObj);
        updateAnswerMappings(existingObj, popDto);
        surveyQuestionDefinitionDao.deleteBySurveyId(existingObj.getId());
        for (SurveyQuestionDefinition questionDefinition : surveyService.getSurveyQuestionDefinitions(popDto)) {
            surveyQuestionDefinitionDao.create(questionDefinition);
        }
        return existingObj;
    }

    @Override
    public Survey createPreserveExisting(Survey existingObj, SurveyPopDto popDto, PortalPopulateContext context) throws IOException {
        int newVersion = surveyService.getNextVersion(popDto.getStableId());
        popDto.setVersion(newVersion);
        return createNew(popDto, context, false);
    }

    @Override
    public Survey createNew(SurveyPopDto popDto, PortalPopulateContext context, boolean overwrite) throws IOException {
        return surveyService.create(popDto);
    }
}
