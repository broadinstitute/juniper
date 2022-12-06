package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.survey.ResponseSnapshotFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.survey.ResponseSnapshot;
import bio.terra.pearl.core.model.survey.SurveyResponse;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class SurveyResponseTests extends BaseSpringBootTest {
    @Autowired
    private SurveyResponseFactory surveyResponseFactory;
    @Autowired
    private ResponseSnapshotFactory responseSnapshotFactory;
    @Autowired
    private SurveyResponseService surveyResponseService;


    @Test
    @Transactional
    public void testSurveyResponseCrud() {
        SurveyResponse surveyResponse = surveyResponseFactory.builderWithDependencies("testSurveyResponseCrud")
                .build();
        SurveyResponse savedResponse = surveyResponseService.create(surveyResponse);
        Assertions.assertNotNull(savedResponse.getId());
        Assertions.assertEquals(surveyResponse.getSurveyId(), savedResponse.getSurveyId());
    }

    @Test
    @Transactional
    public void testSurveyResponseWithSnapshot() {
        String testName = "testSurveyResponseCrud";
        SurveyResponse surveyResponse = surveyResponseFactory.builderWithDependencies(testName)
                .build();
        ResponseSnapshot firstSnapshot = responseSnapshotFactory.builder(testName).build();
        surveyResponse.getSnapshots().add(firstSnapshot);
        SurveyResponse savedResponse = surveyResponseService.create(surveyResponse);
        Assertions.assertNotNull(savedResponse.getId());
        Assertions.assertNotNull(savedResponse.getLastSnapshotId());
        Assertions.assertEquals(savedResponse.getLastSnapshot().getFullData(), firstSnapshot.getFullData());
    }
}
