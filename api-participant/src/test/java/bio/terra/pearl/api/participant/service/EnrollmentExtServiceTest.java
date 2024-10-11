package bio.terra.pearl.api.participant.service;

import static org.junit.jupiter.api.Assertions.*;

import bio.terra.pearl.api.participant.BaseSpringBootTest;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeAndProxy;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.factory.survey.PreEnrollmentSurveyFactory;
import bio.terra.pearl.core.factory.survey.SurveyFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.HubResponse;
import bio.terra.pearl.core.service.participant.EnrolleeRelationService;
import bio.terra.pearl.core.service.participant.EnrolleeService;
import bio.terra.pearl.core.service.participant.ParticipantUserService;
import bio.terra.pearl.core.service.participant.PortalParticipantUserService;
import bio.terra.pearl.core.service.portal.PortalService;
import bio.terra.pearl.core.service.study.StudyService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

class EnrollmentExtServiceTest extends BaseSpringBootTest {

  @Autowired EnrolleeFactory enrolleeFactory;
  @Autowired PortalEnvironmentFactory portalEnvironmentFactory;
  @Autowired StudyEnvironmentFactory studyEnvironmentFactory;
  @Autowired SurveyFactory surveyFactory;
  @Autowired EnrollmentExtService enrollmentExtService;
  @Autowired ParticipantUserService participantUserService;
  @Autowired PortalService portalService;
  @Autowired StudyService studyService;
  @Autowired PreEnrollmentSurveyFactory preEnrollmentSurveyFactory;
  @Autowired EnrolleeRelationService enrolleeRelationService;
  @Autowired PortalParticipantUserService portalParticipantUserService;
  @Autowired EnrolleeService enrolleeService;

  @Test
  @Transactional
  void testEnrollGovernedUser_NewProxy(TestInfo info) {
    // step 1: create portal env
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
    // step 2: create a study env
    StudyEnvironment studyEnvironment =
        studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
    // step 3: enroll a user with a proxy via factory
    EnrolleeAndProxy bundle =
        enrolleeFactory.buildProxyAndGovernedEnrollee(
            getTestName(info), portalEnv, studyEnvironment);
    Enrollee proxyEnrollee = bundle.proxy();

    List<EnrolleeRelation> relationsBefore =
        enrolleeRelationService.findByEnrolleeIdAndRelationType(
            proxyEnrollee.getId(), RelationshipType.PROXY);

    // step 4: enroll a new proxy via service
    HubResponse response =
        enrollmentExtService.enrollGovernedUser(
            participantUserService.find(proxyEnrollee.getParticipantUserId()).get(),
            portalService.find(portalEnv.getPortalId()).get().getShortcode(),
            portalEnv.getEnvironmentName(),
            studyService.find(studyEnvironment.getStudyId()).get().getShortcode(),
            null,
            null);

    List<EnrolleeRelation> relationsAfter =
        enrolleeRelationService.findByEnrolleeIdAndRelationType(
            proxyEnrollee.getId(), RelationshipType.PROXY);

    assertEquals(relationsBefore.size() + 1, relationsAfter.size());
    assertTrue(
        relationsAfter.stream()
            .filter(r -> r.getTargetEnrolleeId().equals(response.getResponse().getId()))
            .findAny()
            .isPresent());
  }

  @Test
  @Transactional
  void testEnrollGovernedUser_ExistingProxyNewStudy(TestInfo info) {
    // step 1: create portal env
    PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(getTestName(info));
    // step 2: create 2 study envs
    StudyEnvironment studyEnvironment1 =
        studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
    StudyEnvironment studyEnvironment2 =
        studyEnvironmentFactory.buildPersisted(portalEnv, getTestName(info));
    // step 3: enroll a user with a proxy via factory
    EnrolleeAndProxy bundle =
        enrolleeFactory.buildProxyAndGovernedEnrollee(
            getTestName(info), portalEnv, studyEnvironment1);
    Enrollee originalProxy = bundle.proxy();
    Enrollee originalGoverned = bundle.governedEnrollee();
    PortalParticipantUser governedPpUser =
        portalParticipantUserService.findForEnrollee(originalGoverned);
    PortalParticipantUser proxyPpUser = bundle.proxyPpUser();

    // step 4: enroll existing proxy to new study via service
    HubResponse<Enrollee> response =
        enrollmentExtService.enrollGovernedUser(
            participantUserService.find(originalProxy.getParticipantUserId()).get(),
            portalService.find(portalEnv.getPortalId()).get().getShortcode(),
            portalEnv.getEnvironmentName(),
            studyService.find(studyEnvironment2.getStudyId()).get().getShortcode(),
            null,
            governedPpUser.getId());

    Enrollee createdProxy =
        enrolleeService
            .findByParticipantUserIdAndStudyEnv(
                proxyPpUser.getParticipantUserId(),
                studyService.find(studyEnvironment2.getStudyId()).get().getShortcode(),
                portalEnv.getEnvironmentName())
            .get();

    Enrollee createdGoverned = response.getResponse();

    // created a new proxy enrollee with the other study environment
    assertNotEquals(createdProxy.getShortcode(), originalProxy.getShortcode());
    assertEquals(studyEnvironment2.getId(), createdProxy.getStudyEnvironmentId());
    assertEquals(proxyPpUser.getProfileId(), createdProxy.getProfileId());
    assertEquals(originalProxy.getParticipantUserId(), createdProxy.getParticipantUserId());
    assertFalse(createdProxy.isSubject());

    // created a new governed enrollee with the other study environment
    assertNotEquals(createdGoverned.getShortcode(), originalGoverned.getShortcode());
    assertEquals(studyEnvironment2.getId(), createdGoverned.getStudyEnvironmentId());
    assertEquals(governedPpUser.getProfileId(), createdGoverned.getProfileId());
    assertEquals(governedPpUser.getParticipantUserId(), createdGoverned.getParticipantUserId());
    assertTrue(createdGoverned.isSubject());
  }
}
