package bio.terra.pearl.core.service.workflow;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;

import java.util.List;
import java.util.Map;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.survey.AnswerFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.model.survey.Answer;
import bio.terra.pearl.core.model.survey.ParsedPreEnrollResponse;
import bio.terra.pearl.core.model.survey.PreEnrollmentResponse;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class EnrollmentServiceTests extends BaseSpringBootTest {
    @Test
    @Transactional
    public void testAnonymousPreEnroll(TestInfo testInfo) throws JsonProcessingException {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(testInfo));
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(testInfo));
        Survey survey = surveyFactory.buildPersisted(getTestName(testInfo));
        studyEnv.setPreEnrollSurveyId(survey.getId());
        studyEnvironmentService.update(studyEnv);
        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();

        List<Answer> answers = AnswerFactory.fromMap(Map.of(
                "qualified", true,
                "areOver18", "yes"
        ));
        ParsedPreEnrollResponse response = ParsedPreEnrollResponse.builder()
                .studyEnvironmentId(studyEnv.getId())
                .answers(answers)
                .qualified(true).build();

        PreEnrollmentResponse savedResponse = enrollmentService.createAnonymousPreEnroll(studyEnv.getId(), survey.getStableId(), survey.getVersion(), response);
        DaoTestUtils.assertGeneratedProperties(savedResponse);
        // confirm it copies over the full data property
        assertThat(savedResponse.getFullData(), containsString("areOver18"));

        // now check that it can be used to enroll the participant
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv,
                getTestName(testInfo));
        String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();
        HubResponse hubResponse = enrollmentService.enroll(studyEnv.getEnvironmentName(), studyShortcode,
                userBundle.user(), userBundle.ppUser(), savedResponse.getId(), false);
        assertThat(hubResponse.getEnrollee(), notNullValue());
    }

    /**
     * confirm that the preEnrollResponse is not required even if the study has a preEnrollSurveyId
     * This is to error on the side of letting users into the study in the event that a strange
     * refresh/oauth redirect has caused us to lose track of their pre-enroll questionnaire.
     * */
    @Test
    @Transactional
    public void testEnrollDoesNotRequirePreEnroll(TestInfo testInfo) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(testInfo));
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(testInfo));
        Survey survey = surveyFactory.buildPersisted(getTestName(testInfo));
        studyEnv.setPreEnrollSurveyId(survey.getId());
        studyEnvironmentService.update(studyEnv);
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv,
                getTestName(testInfo));
        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();

        String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();

        HubResponse hubResponse = enrollmentService.enroll(studyEnv.getEnvironmentName(), studyShortcode,
                userBundle.user(), userBundle.ppUser(), null, false);
        assertThat(hubResponse.getEnrollee(), notNullValue());
    }

    @Test
    @Transactional
    public void testEnrollChecksConfigAllowsEnrollment(TestInfo testInfo) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(testInfo));
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(testInfo));

        StudyEnvironmentConfig studyEnvConfig = studyEnvironmentConfigService.find(studyEnv.getStudyEnvironmentConfigId()).orElseThrow();
        studyEnvConfig.setAcceptingEnrollment(false);
        studyEnvironmentConfigService.update(studyEnvConfig);

        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv,
                getTestName(testInfo));
        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();
        String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            enrollmentService.enroll(studyEnv.getEnvironmentName(), studyShortcode,  userBundle.user(), userBundle.ppUser(),
                   null, false);
        });
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
    private StudyEnvironmentConfigService studyEnvironmentConfigService;
    @Autowired
    private ParticipantUserFactory participantUserFactory;
    @Autowired
    private StudyService studyService;
    @Autowired
    private PortalService portalService;

}
