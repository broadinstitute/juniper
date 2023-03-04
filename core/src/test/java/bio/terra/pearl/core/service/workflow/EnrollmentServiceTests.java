package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.dao.DaoTestUtils;
import bio.terra.pearl.core.factory.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.factory.survey.ResponseDataFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.survey.ParsedPreEnrollResponse;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.model.survey.ResponseData;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Map;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class EnrollmentServiceTests extends BaseSpringBootTest {


    @Test
    public void testAnonymousPreEnroll() throws JsonProcessingException {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testAnonPreEnroll");
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, "testAnonPreEnroll");
        Survey survey = surveyFactory.buildPersisted("testPreEnroll");
        studyEnv.setPreEnrollSurveyId(survey.getId());
        studyEnvironmentService.update(studyEnv);
        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();

        ResponseData responseData = ResponseDataFactory.fromMap(Map.of(
                "qualified", true,
                "areOver18", "yes"
        ));
        ParsedPreEnrollResponse response = ParsedPreEnrollResponse.builder()
                .studyEnvironmentId(studyEnv.getId())
                .parsedData(responseData)
                .qualified(true).build();

        PreEnrollmentResponse savedResponse = enrollmentService.createAnonymousPreEnroll(studyEnv.getId(), survey.getStableId(), survey.getVersion(), response);
        DaoTestUtils.assertGeneratedProperties(savedResponse);
        // confirm it copies over the full data property
        assertThat(savedResponse.getFullData(), containsString("areOver18"));

        // now check that it can be used to enroll the participant
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv,
                "testAnonymousPreEnroll");
        HubResponse hubResponse = enrollmentService.enroll(userBundle.user(), userBundle.ppUser(),
                studyEnv.getEnvironmentName(), studyShortcode, savedResponse.getId());
        assertThat(hubResponse.getEnrollee(), notNullValue());
    }

    @Test
    public void testEnrollRequiresPreEnroll() throws JsonProcessingException {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted("testAnonPreEnroll");
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, "testAnonPreEnroll");
        Survey survey = surveyFactory.buildPersisted("testPreEnroll");
        studyEnv.setPreEnrollSurveyId(survey.getId());
        studyEnvironmentService.update(studyEnv);
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv,
                "testEnrollRequiresPreEnroll");
        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();


        Exception e = assertThrows(IllegalArgumentException.class, () -> {
            HubResponse hubResponse = enrollmentService.enroll(userBundle.user(), userBundle.ppUser(),
                    studyEnv.getEnvironmentName(), studyShortcode, null);
        });
        assertThat(e.getMessage(), containsString("pre-enrollment survey is required"));
    }

    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private SurveyFactory surveyFactory;
    @Autowired
    private StudyEnvironmentService studyEnvironmentService;
    @Autowired
    private ParticipantUserFactory participantUserFactory;
    @Autowired
    private StudyService studyService;

}
