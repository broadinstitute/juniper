package bio.terra.pearl.core.service.survey;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.survey.ResponseSnapshotFactory;
import bio.terra.pearl.core.factory.survey.SurveyResponseFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.study.StudyEnvironmentSurveyService;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class SurveyResponseServiceTests extends BaseSpringBootTest {
    @Autowired
    private SurveyResponseFactory surveyResponseFactory;
    @Autowired
    private ResponseSnapshotFactory responseSnapshotFactory;
    @Autowired
    private SurveyResponseService surveyResponseService;
    @Autowired
    private EnrolleeService enrolleeService;
    @Autowired
    private SurveyService surveyService;
    @Autowired
    private StudyEnvironmentSurveyService studyEnvironmentSurveyService;

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

        // attach the form to a study environment so we can test full retrieval
        Enrollee enrollee = enrolleeService.find(savedResponse.getEnrolleeId()).get();
        Survey survey = surveyService.find(savedResponse.getSurveyId()).get();
        studyEnvironmentSurveyService.create(StudyEnvironmentSurvey.builder()
                .surveyId(survey.getId())
                .studyEnvironmentId(enrollee.getStudyEnvironmentId())
                .surveyOrder(2)
                .build());

        SurveyWithResponse survWithResponse = surveyResponseService.findWithActiveResponse(enrollee.getStudyEnvironmentId(),
                survey.getStableId(), survey.getVersion(), enrollee, null);
        assertThat(survWithResponse, notNullValue());
        assertThat(survWithResponse.surveyResponse(), notNullValue());
        assertThat(survWithResponse.surveyResponse().getLastSnapshot().getFullData(), equalTo(firstSnapshot.getFullData()));
        assertThat(survWithResponse.studyEnvironmentSurvey().getSurveyOrder(), equalTo(2));
        assertThat(survWithResponse.studyEnvironmentSurvey().getSurvey().getId(),
                equalTo(survey.getId()));
    }
}
