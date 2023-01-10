package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.survey.StudyEnvironmentSurvey;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.populate.dao.SurveyPopulateDao;
import bio.terra.pearl.populate.dto.survey.StudyEnvironmentSurveyPopDto;
import bio.terra.pearl.populate.dto.survey.SurveyPopDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/** populator for surveys and consent forms */
@Service
public class SurveyPopulator extends Populator<Survey> {
    private SurveyService surveyService;
    private PortalService portalService;
    private SurveyPopulateDao surveyPopulateDao;

    public SurveyPopulator(SurveyService surveyService,
                           ObjectMapper objectMapper, FilePopulateService filePopulateService,
                           PortalService portalService,
                           SurveyPopulateDao surveyPopulateDao) {
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
        Optional<Survey> existingSurveyOpt = fetchFromPopDto(surveyPopDto);

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

    public StudyEnvironmentSurvey convertConfiguredSurvey(StudyEnvironmentSurveyPopDto configuredSurveyDto, int index) {
        StudyEnvironmentSurvey configuredSurvey = new StudyEnvironmentSurvey();
        BeanUtils.copyProperties(configuredSurveyDto, configuredSurvey);
        Survey survey = surveyService.findByStableId(configuredSurveyDto.getSurveyStableId(),
                configuredSurveyDto.getSurveyVersion()).get();
        configuredSurvey.setSurveyId(survey.getId());
        configuredSurvey.setSurveyOrder(index);
        return configuredSurvey;
    }

    public Optional<Survey> fetchFromPopDto(SurveyPopDto surveyPopDto) {
        return surveyService.findByStableId(surveyPopDto.getStableId(), surveyPopDto.getVersion());
    }
}
