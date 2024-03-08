package bio.terra.pearl.core.service.participant;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.WithdrawnEnrollee;
import bio.terra.pearl.core.model.workflow.HubResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class WithdrawnEnrolleeServiceTests extends BaseSpringBootTest {
  @Autowired
  private EnrolleeFactory enrolleeFactory;
  @Autowired
  private EnrolleeService enrolleeService;
  @Autowired
  private WithdrawnEnrolleeService withdrawnEnrolleeService;

  @Test
  @Transactional
  public void testWithdraw(TestInfo info) throws Exception {
    Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info));
    DaoTestUtils.assertGeneratedProperties(enrollee);
    WithdrawnEnrollee withdrawnEnrollee = withdrawnEnrolleeService.withdrawEnrollee(enrollee);
    DaoTestUtils.assertGeneratedProperties(withdrawnEnrollee);

    assertThat(enrolleeService.find(enrollee.getId()).isPresent(), equalTo(false));
    assertThat(withdrawnEnrolleeService.find(withdrawnEnrollee.getId()).isPresent(), equalTo(true));
    assertThat(withdrawnEnrolleeService.isWithdrawn(enrollee.getShortcode()), equalTo(true));
  }
  @Test
  @Transactional
  public void testWithdrawProxyEnrollee(TestInfo info) {
    EnrolleeFactory.EnrolleeAndProxy enrolleeAndProxy = enrolleeFactory.buildProxyAndGovernedEnrollee(getTestName(info), "proxy-email@test.com");
    Enrollee proxyEnrollee = enrolleeAndProxy.proxy();
    Enrollee governedEnrollee = enrolleeAndProxy.governedEnrollee();
    DaoTestUtils.assertGeneratedProperties(proxyEnrollee);
    WithdrawnEnrollee withdrawnEnrollee = withdrawnEnrolleeService.withdrawEnrollee(proxyEnrollee);
    DaoTestUtils.assertGeneratedProperties(withdrawnEnrollee);
    assertThat(proxyEnrollee.getShortcode().equals(withdrawnEnrollee.getShortcode()), equalTo(true));

    assertThat(enrolleeService.find(proxyEnrollee.getId()).isPresent(), equalTo(false));
    assertThat(enrolleeService.find(governedEnrollee.getId()).isPresent(), equalTo(false));
    assertThat(withdrawnEnrolleeService.find(withdrawnEnrollee.getId()).isPresent(), equalTo(true));
    assertThat(withdrawnEnrolleeService.isWithdrawn(proxyEnrollee.getShortcode()), equalTo(true));
    assertThat(withdrawnEnrolleeService.isWithdrawn(governedEnrollee.getShortcode()), equalTo(true));
  }
}
