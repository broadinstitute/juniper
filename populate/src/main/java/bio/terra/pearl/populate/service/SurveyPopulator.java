package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.dao.survey.AnswerMappingDao;
import bio.terra.pearl.core.dao.survey.SurveyQuestionDefinitionDao;
import bio.terra.pearl.core.model.Versioned;
import bio.terra.pearl.core.model.survey.AnswerMapping;
import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
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
public class SurveyPopulator extends Populator<Survey, PortalPopulateContext> {
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
    public Survey populateFromString(String fileString, PortalPopulateContext context) throws IOException {
        SurveyPopDto surveyPopDto = objectMapper.readValue(fileString, SurveyPopDto.class);
        String newContent = surveyPopDto.getJsonContent().toString();
        surveyPopDto.setContent(newContent);
        UUID portalId = portalService.findOneByShortcode(context.getPortalShortcode()).get().getId();
        surveyPopDto.setPortalId(portalId);
        Optional<Survey> existingSurveyOpt = fetchFromPopDto(surveyPopDto);

        if (existingSurveyOpt.isPresent()) {
            Survey existingSurvey = existingSurveyOpt.get();
            // don't delete the survey, since it may have other entities attached to it. Just mod the content
            existingSurvey.setContent(surveyPopDto.getContent());
            existingSurvey.setName(surveyPopDto.getName());
            surveyPopulateDao.update(existingSurvey);
            answerMappingDao.deleteBySurveyId(existingSurvey.getId());
            existingSurvey.getAnswerMappings().clear();
            for (AnswerMapping answerMapping : surveyPopDto.getAnswerMappings()) {
                answerMapping.setSurveyId(existingSurvey.getId());
                existingSurvey.getAnswerMappings().add(answerMappingDao.create(answerMapping));
            }
            surveyQuestionDefinitionDao.deleteBySurveyId(existingSurvey.getId());
            surveyService.createSurveyDataDictionary(surveyPopDto);

            return existingSurvey;
        }
        return surveyService.create(surveyPopDto);
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

    public Optional<Survey> fetchFromPopDto(Versioned popDto) {
        return surveyService.findByStableId(popDto.getStableId(), popDto.getVersion());
    }
}
