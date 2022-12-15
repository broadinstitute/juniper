package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.populate.dao.SurveyPopulateDao;
import bio.terra.pearl.populate.dto.survey.SurveyBatchPopDto;
import bio.terra.pearl.populate.dto.survey.SurveyBatchSurveyPopDto;
import bio.terra.pearl.populate.dto.survey.SurveyPopDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Service
public class SurveyPopulator extends Populator<Survey> {
    private SurveyService surveyService;
    private PortalService portalService;
    private SurveyPopulateDao surveyPopulateDao;

    public SurveyPopulator(SurveyService surveyService,
                           ObjectMapper objectMapper, FilePopulateService filePopulateService, PortalService portalService, SurveyPopulateDao surveyPopulateDao) {
        this.portalService = portalService;
        this.surveyPopulateDao = surveyPopulateDao;
        this.objectMapper = objectMapper;
        this.filePopulateService = filePopulateService;
        this.surveyService = surveyService;
    }

    @Override
    public Survey populateFromString(String fileString, FilePopulateConfig config) throws IOException {
        SurveyPopDto surveyPopDto = objectMapper.readValue(fileString, SurveyPopDto.class);
        String newContent = surveyPopDto.getJsonContent().toString();
        surveyPopDto.setContent(newContent);
        UUID portalId = portalService.findOneByShortcode(config.getPortalShortcode()).get().getId();
        surveyPopDto.setPortalId(portalId);
        Optional<Survey> existingSurveyOpt = surveyService.findByStableId(surveyPopDto.getStableId(), surveyPopDto.getVersion());

        if (existingSurveyOpt.isPresent()) {
            Survey existingSurvey = existingSurveyOpt.get();
            // don't delete the survey, since it may have other entities attached to it. Just mod the content
            existingSurvey.setContent(surveyPopDto.getContent());
            existingSurvey.setName(surveyPopDto.getName());
            surveyPopulateDao.update(existingSurvey);
            return existingSurvey;
        }
        return surveyService.create(surveyPopDto);
    }

    public void attachSurveyBatchSurveys(SurveyBatchPopDto batchDto) {
        for (int i = 0; i < batchDto.getSurveyBatchSurveyDtos().size(); i++) {
            SurveyBatchSurveyPopDto sbsPop = batchDto.getSurveyBatchSurveyDtos().get(i);
            Survey survey = surveyService.findByStableId(sbsPop.getSurveyStableId(), sbsPop.getSurveyVersion()).get();
            sbsPop.setSurveyId(survey.getId());
            sbsPop.setSurveyOrder(i);
        }
    }
}
