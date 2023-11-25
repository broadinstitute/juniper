package bio.terra.pearl.populate.service.extract;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.service.survey.SurveyService;
import bio.terra.pearl.populate.dto.survey.SurveyPopDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SurveyExtractor {
    private final SurveyService surveyService;
    private final ObjectMapper objectMapper;

    public SurveyExtractor(SurveyService surveyService, @Qualifier("extractionObjectMapper") ObjectMapper objectMapper) {
        this.surveyService = surveyService;
        this.objectMapper = objectMapper;
    }

    /** writes all versions of all surveys to the zip file */
    public void writeSurveys(Portal portal, ExportPopulateContext context) {
        List<Survey> surveys = surveyService.findByPortalId(portal.getId());
        for (Survey survey : surveys) {
            surveyService.attachAnswerMappings(survey);
            writeSurvey(survey, context);
        }
    }

    public void writeSurvey(Survey survey, ExportPopulateContext context) {
        SurveyPopDto surveyPopDto = new SurveyPopDto();
        BeanUtils.copyProperties(survey, surveyPopDto, "id", "portalId", "content");
        try {
            surveyPopDto.setJsonContent(objectMapper.readTree(survey.getContent()));
            String fileString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(surveyPopDto);
            context.writeFileForEntity(fileNameForSurvey(survey), fileString, survey.getId());
            context.getPortalPopDto().getSurveyFiles().add(fileNameForSurvey(survey));
        } catch (Exception e) {
            throw new RuntimeException("Error writing survey %s-%s to json".formatted(survey.getStableId(), survey.getVersion()), e);
        }
    }

    protected String fileNameForSurvey(Survey survey) {
        return "surveys/%s-%s.json".formatted(survey.getStableId(), survey.getVersion());
    }
}
