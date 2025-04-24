package bio.terra.pearl.core.service.workflow;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeAndProxy;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.participant.ParticipantUserFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.survey.AnswerFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.StudyEnvironmentConfigService;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import bio.terra.pearl.core.service.study.StudyService;
import bio.terra.pearl.core.service.survey.SurveyResponseService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class EnrollmentServiceTests extends BaseSpringBootTest {
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
    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private PortalParticipantUserService portalParticipantUserService;
    @Autowired
    private ParticipantUserService participantUserService;
    @Autowired
    private EnrolleeService enrolleeService;
    @Autowired
    private SurveyResponseService surveyResponseService;

    @Test
    @Transactional
    public void testAnonymousPreEnroll(TestInfo testInfo) throws JsonProcessingException {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(testInfo));
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(testInfo));
        Survey survey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(testInfo))
                .surveyType(SurveyType.PRE_ENROLL)
                .portalId(portalEnv.getPortalId()));

        surveyFactory.attachToEnv(survey, studyEnv.getId(), true);
        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();

        List<Answer> answers = AnswerFactory.fromMap(Map.of(
                "qualified", true,
                "areOver18", "yes"
        ));
        ParsedPreEnrollResponse response = ParsedPreEnrollResponse.builder()
                .studyEnvironmentId(studyEnv.getId())
                .answers(answers)
                .qualified(true).build();

        PreEnrollmentResponse savedResponse = enrollmentService.createAnonymousPreEnroll(survey.getPortalId(), studyEnv.getId(), survey.getStableId(), survey.getVersion(), response);
        DaoTestUtils.assertGeneratedProperties(savedResponse);
        // confirm it copies over the full data property
        assertThat(savedResponse.getFullData(), containsString("areOver18"));

        // now check that it can be used to enroll the participant
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv,
                getTestName(testInfo));
        String portalShortcode = portalService.find(portalEnv.getPortalId()).get().getShortcode();
        HubResponse hubResponse = enrollmentService.enroll(userBundle.ppUser(), studyEnv.getEnvironmentName(), studyShortcode,
                userBundle.user(), userBundle.ppUser(), savedResponse.getId(), false);
        assertThat(hubResponse.getEnrollee(), notNullValue());

        // now check that the response is backfilled to a SurveyResponse
        List<SurveyResponse> responses = surveyResponseService.findByEnrolleeId(hubResponse.getEnrollee().getId());
        assertThat(responses, hasSize(1));
        assertThat(responses.get(0).getSurveyId(), equalTo(survey.getId()));
        List<Answer> responseAnswers = surveyResponseService.findOneWithAnswers(responses.get(0).getId()).get().getAnswers();
        assertThat(answers, hasSize(2));

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
        Survey survey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(testInfo))
                .surveyType(SurveyType.PRE_ENROLL)
                .portalId(portalEnv.getPortalId()));
        surveyFactory.attachToEnv(survey, studyEnv.getId(), true);
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv,
                getTestName(testInfo));
        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();

        HubResponse hubResponse = enrollmentService.enroll(userBundle.ppUser(), studyEnv.getEnvironmentName(), studyShortcode,
                userBundle.user(), userBundle.ppUser(), null, false);
        assertThat(hubResponse.getEnrollee(), notNullValue());
    }

    @Test
    @Transactional
    public void testEnrollRequiresPreEnrollIfSurveyRequired(TestInfo testInfo) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(testInfo));
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(testInfo));
        Survey survey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(testInfo))
                .surveyType(SurveyType.PRE_ENROLL)
                .required(true)
                .portalId(portalEnv.getPortalId()));
        surveyFactory.attachToEnv(survey, studyEnv.getId(), true);
        ParticipantUserFactory.ParticipantUserAndPortalUser userBundle = participantUserFactory.buildPersisted(portalEnv,
                getTestName(testInfo));
        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();


        // subject / governed user enrollment should fail with null pre-enroll
        assertThrows(
                IllegalArgumentException.class,
                () -> {
                    enrollmentService.enroll(userBundle.ppUser(), studyEnv.getEnvironmentName(), studyShortcode,
                            userBundle.user(), userBundle.ppUser(), null, true);
                });

        // proxy enrollment should work; pre-enrolls don't matter for proxies
        enrollmentService.enroll(userBundle.ppUser(), studyEnv.getEnvironmentName(), studyShortcode,
                userBundle.user(), userBundle.ppUser(), null, false);


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
        assertThrows(IllegalArgumentException.class, () -> {
            enrollmentService.enroll(userBundle.ppUser(), studyEnv.getEnvironmentName(), studyShortcode, userBundle.user(), userBundle.ppUser(),
                    null, false);
        });
    }

    @Test
    @Transactional
    public void testEnrollProxy(TestInfo testInfo) throws JsonProcessingException {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(testInfo));
        StudyEnvironment studyEnv = studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(testInfo));

        EnrolleeAndProxy enrolleeAndProxy = enrolleeFactory.buildProxyAndGovernedEnrollee(getTestName(testInfo), portalEnv, studyEnv);
        Enrollee proxy = enrolleeAndProxy.proxy();
        PortalParticipantUser ppUser = portalParticipantUserService.findForEnrollee(proxy);
        ParticipantUser user = participantUserService.find(ppUser.getParticipantUserId()).orElseThrow();

        String studyShortcode = studyService.find(studyEnv.getStudyId()).get().getShortcode();

        Survey survey = surveyFactory.buildPersisted(surveyFactory.builder(getTestName(testInfo))
                .surveyType(SurveyType.PRE_ENROLL)
                .portalId(portalEnv.getPortalId()));
        surveyFactory.attachToEnv(survey, studyEnv.getId(), true);

        List<Answer> answers = AnswerFactory.fromMap(Map.of(
                "qualified", true,
                "areOver18", "yes"
        ));
        ParsedPreEnrollResponse response = ParsedPreEnrollResponse.builder()
                .studyEnvironmentId(studyEnv.getId())
                .answers(answers)
                .qualified(true).build();

        PreEnrollmentResponse savedResponse = enrollmentService.createAnonymousPreEnroll(survey.getPortalId(), studyEnv.getId(), survey.getStableId(), survey.getVersion(), response);

        // now, enroll subject as proxy
        enrollmentService.enroll(ppUser, studyEnv.getEnvironmentName(), studyShortcode, user, ppUser,
                savedResponse.getId(), true);

        Enrollee proxyAfterSubjectEnroll = enrolleeService.find(proxy.getId()).orElseThrow();
        assertThat(proxyAfterSubjectEnroll.isSubject(), equalTo(true));
        assertThat(proxyAfterSubjectEnroll.getPreEnrollmentResponseId(), equalTo(savedResponse.getId()));
    }

}
