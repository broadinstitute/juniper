package bio.terra.pearl.core.service.export;

import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.survey.SurveyQuestionDefinition;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class SurveyFormatterTests {
    @Autowired
    ObjectMapper objectMapper;
    @Test
    public void testToStringMap() throws Exception {
        Survey survey = Survey.builder()
                .id(UUID.randomUUID())
                .stableId("surveyA")
                .version(1)
                .build();
        SurveyQuestionDefinition questionDef = SurveyQuestionDefinition.builder()
                .questionStableId("q1")
                .questionType("text")
                .exportOrder(1)
                .build();
        var moduleExportInfo = new SurveyFormatter(objectMapper)
                .getModuleExportInfo(survey, List.of(questionDef));
        SurveyResponse response = SurveyResponse.builder()
                .id(UUID.randomUUID())
                .surveyId(survey.getId())
                .build();
        Answer answer = Answer.builder()
                .surveyStableId(survey.getStableId())
                .questionStableId("q1")
                .surveyResponseId(response.getId())
                .stringValue("easyValue")
                .build();
        EnrolleeExportData enrolleeExportData = new EnrolleeExportData(null, null,
                List.of(answer), null, List.of(response));
        Map<String, String> valueMap = moduleExportInfo.toStringMap(enrolleeExportData);

        assertThat(valueMap.get("surveyA.q1"), equalTo("easyValue"));
        assertThat(valueMap.get("surveyA.complete"), equalTo("false"));
    }
}
