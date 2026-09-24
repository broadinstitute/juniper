package bio.terra.pearl.api.participant.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import bio.terra.common.exception.NotFoundException;
import bio.terra.pearl.api.participant.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentBundle;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeAndProxy;
import bio.terra.pearl.core.factory.participant.EnrolleeBundle;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.ParticipantUser;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.survey.*;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.participant.ProfileService;
import bio.terra.pearl.core.service.workflow.ParticipantTaskService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class SurveyResponseExtServiceTest extends BaseSpringBootTest {

  @Autowired SurveyResponseExtService surveyResponseExtService;
  @Autowired StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired SurveyFactory surveyFactory;
  @Autowired EnrolleeFactory enrolleeFactory;
  @Autowired ParticipantUserService participantUserService;
  @Autowired ParticipantTaskService participantTaskService;
  @Autowired PortalParticipantUserService portalParticipantUserService;
  @Autowired ProfileService profileService;

  @Test
  @Transactional
  public void testProxyProfileMapping(TestInfo info) {

    StudyEnvironmentBundle studyEnvironmentBundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);

    List<AnswerMapping> answerMappings =
        List.of(
            AnswerMapping.builder()
                .questionStableId("proxyGivenName")
                .targetType(AnswerMappingTargetType.PROXY_PROFILE)
                .mapType(AnswerMappingMapType.STRING_TO_STRING)
                .targetField("givenName")
                .build());

    Survey survey =
        surveyFactory.buildPersisted(
            surveyFactory
                .builder(getTestName(info))
                .answerMappings(answerMappings)
                .portalId(studyEnvironmentBundle.getPortal().getId())
                .content(
                    "{\"pages\":[{\"elements\":[{\"type\":\"text\",\"name\":\"proxyGivenName\",\"title\":\"What is your name?\"}]}]}"));

    surveyFactory.attachToEnv(survey, studyEnvironmentBundle.getStudyEnv().getId(), true);

    EnrolleeAndProxy enrolleeAndProxy =
        enrolleeFactory.buildProxyAndGovernedEnrollee(
            getTestName(info),
            studyEnvironmentBundle.getPortalEnv(),
            studyEnvironmentBundle.getStudyEnv());

    SurveyResponse response =
        SurveyResponse.builder()
            .surveyId(survey.getId())
            .enrolleeId(enrolleeAndProxy.governedEnrollee().getId())
            .answers(
                List.of(
                    Answer.builder()
                        .surveyVersion(survey.getVersion())
                        .surveyStableId(survey.getStableId())
                        .questionStableId("proxyGivenName")
                        .stringValue("John")
                        .build()))
            .build();

    ParticipantUser proxyUser =
        participantUserService.find(enrolleeAndProxy.proxy().getParticipantUserId()).get();

    PortalParticipantUser governedPpUser =
        portalParticipantUserService.findForEnrollee(enrolleeAndProxy.governedEnrollee());
    ParticipantTask task =
        participantTaskService
            .findTaskForActivity(
                governedPpUser.getId(),
                studyEnvironmentBundle.getStudyEnv().getId(),
                survey.getStableId())
            .get();

    surveyResponseExtService.updateResponse(
        proxyUser,
        studyEnvironmentBundle.getPortal().getShortcode(),
        EnvironmentName.sandbox,
        response,
        enrolleeAndProxy.governedEnrollee().getShortcode(),
        task.getId());

    Profile proxyProfile = profileService.find(enrolleeAndProxy.proxy().getProfileId()).get();
    assertEquals("John", proxyProfile.getGivenName());
  }

  @Test
  @Transactional
  public void testParticipantCannotAccessStaffForm(TestInfo info) {
    StudyEnvironmentBundle studyEnvBundle =
        studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
    Survey adminForm =
        surveyFactory.buildPersisted(
            surveyFactory
                .builder(getTestName(info))
                .portalId(studyEnvBundle.getPortal().getId())
                .surveyType(SurveyType.ADMIN));
    surveyFactory.attachToEnv(adminForm, studyEnvBundle.getStudyEnv().getId(), true);
    EnrolleeBundle enrolleeBundle =
        enrolleeFactory.buildWithPortalUser(
            getTestName(info), studyEnvBundle.getPortalEnv(), studyEnvBundle.getStudyEnv());
    ParticipantTask adminTask =
        participantTaskService.create(
            ParticipantTask.builder()
                .enrolleeId(enrolleeBundle.enrollee().getId())
                .portalParticipantUserId(enrolleeBundle.portalParticipantUser().getId())
                .studyEnvironmentId(studyEnvBundle.getStudyEnv().getId())
                .taskType(TaskType.ADMIN_FORM)
                .targetStableId(adminForm.getStableId())
                .targetAssignedVersion(adminForm.getVersion())
                .status(TaskStatus.NEW)
                .build(),
            getAuditInfo(info));

    String portalShortcode = studyEnvBundle.getPortal().getShortcode();
    String enrolleeShortcode = enrolleeBundle.enrollee().getShortcode();

    assertThrows(
        NotFoundException.class,
        () ->
            surveyResponseExtService.findOrCreateWithActiveResponse(
                portalShortcode,
                studyEnvBundle.getStudy().getShortcode(),
                EnvironmentName.sandbox.name(),
                adminForm.getStableId(),
                adminForm.getVersion(),
                enrolleeShortcode,
                enrolleeBundle.participantUser().getId(),
                adminTask.getId()));

    SurveyResponse response =
        SurveyResponse.builder()
            .surveyId(adminForm.getId())
            .enrolleeId(enrolleeBundle.enrollee().getId())
            .complete(true)
            .build();
    assertThrows(
        NotFoundException.class,
        () ->
            surveyResponseExtService.updateResponse(
                enrolleeBundle.participantUser(),
                portalShortcode,
                EnvironmentName.sandbox,
                response,
                enrolleeShortcode,
                adminTask.getId()));
    assertEquals(TaskStatus.NEW, participantTaskService.find(adminTask.getId()).get().getStatus());
  }
}
