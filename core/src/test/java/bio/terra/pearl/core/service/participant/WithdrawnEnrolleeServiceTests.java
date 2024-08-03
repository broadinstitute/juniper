package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.EnrolleeRelation;
import bio.terra.pearl.core.model.participant.RelationshipType;
import bio.terra.pearl.core.model.participant.WithdrawnEnrollee;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class WithdrawnEnrolleeServiceTests extends BaseSpringBootTest {
  @Autowired
  private EnrolleeFactory enrolleeFactory;
  @Autowired
  private EnrolleeService enrolleeService;
  @Autowired
  private EnrolleeRelationService enrolleeRelationService;
  @Autowired
  private WithdrawnEnrolleeService withdrawnEnrolleeService;
  @Autowired
  private StudyEnvironmentFactory studyEnvironmentFactory;


  @Test
  @Transactional
  public void testWithdraw(TestInfo info) {
    Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));
    DaoTestUtils.assertGeneratedProperties(enrollee);
    WithdrawnEnrollee withdrawnEnrollee = withdrawnEnrolleeService.withdrawEnrollee(enrollee, getAuditInfo(info));
    DaoTestUtils.assertGeneratedProperties(withdrawnEnrollee);

    assertThat(enrolleeService.find(enrollee.getId()).isPresent(), equalTo(false));
    assertThat(withdrawnEnrolleeService.find(withdrawnEnrollee.getId()).isPresent(), equalTo(true));
    assertThat(withdrawnEnrolleeService.isWithdrawn(enrollee.getShortcode()), equalTo(true));

    // confirm we can fetch by study environment without returning all the data
    List<WithdrawnEnrollee> withdrawnEnrollees = withdrawnEnrolleeService.findByStudyEnvironmentIdNoData(enrollee.getStudyEnvironmentId());
    assertThat(withdrawnEnrollees.size(), equalTo(1));
    assertThat(withdrawnEnrollees.get(0).getShortcode(), equalTo(enrollee.getShortcode()));
    assertThat(withdrawnEnrollees.get(0).getEnrolleeData(), nullValue());
  }
  @Test
  @Transactional
  public void testWithdrawProxyEnrollee(TestInfo info) {
    EnrolleeFactory.EnrolleeAndProxy enrolleeAndProxy = enrolleeFactory.buildProxyAndGovernedEnrollee(getTestName(info), "proxy-email@test.com");
    Enrollee proxyEnrollee = enrolleeAndProxy.proxy();
    Enrollee governedEnrollee = enrolleeAndProxy.governedEnrollee();
    DaoTestUtils.assertGeneratedProperties(proxyEnrollee);
    WithdrawnEnrollee withdrawnEnrollee = withdrawnEnrolleeService.withdrawEnrollee(governedEnrollee, getAuditInfo(info));
    DaoTestUtils.assertGeneratedProperties(withdrawnEnrollee);
    assertThat(governedEnrollee.getShortcode().equals(withdrawnEnrollee.getShortcode()), equalTo(true));

    assertThat(enrolleeService.find(proxyEnrollee.getId()).isPresent(), equalTo(false));
    assertThat(enrolleeService.find(governedEnrollee.getId()).isPresent(), equalTo(false));
    assertThat(withdrawnEnrolleeService.find(withdrawnEnrollee.getId()).isPresent(), equalTo(true));
    assertThat(withdrawnEnrolleeService.isWithdrawn(proxyEnrollee.getShortcode()), equalTo(true));
    assertThat(withdrawnEnrolleeService.isWithdrawn(governedEnrollee.getShortcode()), equalTo(true));
  }

  @Test
  @Transactional
  public void testWithdrawSubjectThatProxies(TestInfo info) {
    // potential edge case: withdrawing a subject that is also a proxy should withdraw the proxy
    // but then recreate a non-subject enrollee so that they can keep proxying.

    StudyEnvironmentFactory.StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
    StudyEnvironment studyEnvironment = studyEnvBundle.getStudyEnv();
    PortalEnvironment portalEnvironment = studyEnvBundle.getPortalEnv();


    EnrolleeFactory.EnrolleeBundle proxyBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnvironment, studyEnvironment);
    EnrolleeFactory.EnrolleeBundle governedBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnvironment, studyEnvironment);

    Enrollee proxyEnrollee = proxyBundle.enrollee();
    Enrollee governedEnrollee = governedBundle.enrollee();

    enrolleeRelationService.create(
            EnrolleeRelation
                    .builder()
                    .enrolleeId(proxyEnrollee.getId())
                    .targetEnrolleeId(governedEnrollee.getId())
                    .relationshipType(RelationshipType.PROXY)
                    .beginDate(Instant.now())
                    .build(),
            getAuditInfo(info)
    );

    WithdrawnEnrollee withdrawnEnrollee = withdrawnEnrolleeService.withdrawEnrollee(proxyEnrollee, getAuditInfo(info));
    DaoTestUtils.assertGeneratedProperties(withdrawnEnrollee);
    assertThat(proxyEnrollee.getShortcode().equals(withdrawnEnrollee.getShortcode()), equalTo(true));

    assertThat(withdrawnEnrolleeService.isWithdrawn(proxyEnrollee.getShortcode()), equalTo(true));
    // does not withdraw the governed enrollee
    assertThat(withdrawnEnrolleeService.isWithdrawn(governedEnrollee.getShortcode()), equalTo(false));

    // creates a new enrollee for proxy
    Enrollee newProxyEnrollee = enrolleeService.findByParticipantUserIdAndStudyEnv(
            proxyEnrollee.getParticipantUserId(),
            studyEnvBundle.getStudy().getShortcode(),
            studyEnvironment.getEnvironmentName()
    ).orElseThrow();

    // same as old enrollee, but new shortcode and not a subject
    assertThat(newProxyEnrollee.getShortcode().equals(proxyEnrollee.getShortcode()), equalTo(false));
    assertThat(newProxyEnrollee.getParticipantUserId(), equalTo(proxyEnrollee.getParticipantUserId()));
    assertThat(newProxyEnrollee.getStudyEnvironmentId(), equalTo(proxyEnrollee.getStudyEnvironmentId()));
    assertThat(newProxyEnrollee.isSubject(), equalTo(false));

    List<EnrolleeRelation> enrolleeRelations = enrolleeRelationService.findByEnrolleeIdAndRelationType(newProxyEnrollee.getId(), RelationshipType.PROXY);

    // recreates the proxy relationship
    assertThat(enrolleeRelations.size(), equalTo(1));
    assertThat(enrolleeRelations.get(0).getEnrolleeId(), equalTo(newProxyEnrollee.getId()));
    assertThat(enrolleeRelations.get(0).getTargetEnrolleeId(), equalTo(governedEnrollee.getId()));
  }

  @Test
  @Transactional
  public void testWithdrawGovernedUserWithProxyThatIsSubject(TestInfo info) {
    // potential edge case: withdrawing a governed user that is being proxied by a subject should withdraw ONLY the governed user
    StudyEnvironmentFactory.StudyEnvironmentBundle studyEnvBundle = studyEnvironmentFactory.buildBundle(getTestName(info), EnvironmentName.sandbox);
    StudyEnvironment studyEnvironment = studyEnvBundle.getStudyEnv();
    PortalEnvironment portalEnvironment = studyEnvBundle.getPortalEnv();


    EnrolleeFactory.EnrolleeBundle proxyBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnvironment, studyEnvironment);
    EnrolleeFactory.EnrolleeBundle governedBundle = enrolleeFactory.buildWithPortalUser(getTestName(info), portalEnvironment, studyEnvironment);

    Enrollee proxyEnrollee = proxyBundle.enrollee();
    Enrollee governedEnrollee = governedBundle.enrollee();

    enrolleeRelationService.create(
            EnrolleeRelation
                    .builder()
                    .enrolleeId(proxyEnrollee.getId())
                    .targetEnrolleeId(governedEnrollee.getId())
                    .relationshipType(RelationshipType.PROXY)
                    .beginDate(Instant.now())
                    .build(),
            getAuditInfo(info)
    );

    withdrawnEnrolleeService.withdrawEnrollee(governedEnrollee, getAuditInfo(info));

    assertThat(withdrawnEnrolleeService.isWithdrawn(governedEnrollee.getShortcode()), equalTo(true));
    assertThat(withdrawnEnrolleeService.isWithdrawn(proxyEnrollee.getShortcode()), equalTo(false));
  }
}
