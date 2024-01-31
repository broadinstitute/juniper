package bio.terra.pearl.core.service.kit.pepper;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.EnvironmentFactory;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.StudyFactory;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.notNullValue;

class StubPepperDSMClientTest extends BaseSpringBootTest {
    @Autowired
    private StubPepperDSMClient stubPepperDSMClient;

    @Transactional
    @Test
    public void testFetchKitStatus(TestInfo info) throws Exception {
        // Arrange
        Study study = studyFactory.buildPersisted(getTestName(info));
        environmentFactory.buildPersisted(getTestName(info), EnvironmentName.sandbox);
        StudyEnvironment studyEnvironment = studyEnvironmentService.create(
                studyEnvironmentFactory.builder(getTestName(info))
                        .studyId(study.getId())
                        .environmentName(EnvironmentName.sandbox)
                        .studyEnvironmentConfig(new StudyEnvironmentConfig())
                        .build());
        Enrollee enrollee = enrolleeFactory.buildPersisted(getTestName(info), studyEnvironment);
        KitRequest kit = kitRequestFactory.buildPersisted(getTestName(info), enrollee);

        // Act
        PepperKit kitStatus = stubPepperDSMClient.fetchKitStatus(kit.getId());

        // Assert
        assertThat(kitStatus, hasProperty("currentStatus", equalTo("SENT")));
    }

    @Transactional
    @Test
    public void testFetchKitStatusByStudy(TestInfo testInfo) throws Exception {
        String testName = getTestName(testInfo);
        Study study = studyFactory.buildPersisted(testName);
        environmentFactory.buildPersisted(testName, EnvironmentName.sandbox);
        StudyEnvironment studyEnvironment = studyEnvironmentService.create(
                studyEnvironmentFactory.builder(testName)
                        .studyId(study.getId())
                        .environmentName(EnvironmentName.sandbox)
                        .studyEnvironmentConfig(new StudyEnvironmentConfig())
                        .build());
        Enrollee enrollee = enrolleeFactory.buildPersisted(testName, studyEnvironment);
        // when current request status is SENT, the stub client bumps that to RECEIVED
        KitRequest kitRequest = kitRequestFactory.buildPersisted(testName, enrollee, PepperKitStatus.SENT);

        // Act
        Collection<PepperKit> kitStatuses = stubPepperDSMClient.fetchKitStatusByStudy(study.getShortcode());

        // Assert
        assertThat(kitStatuses.size(), equalTo(1));
        PepperKit pepperKit = kitStatuses.stream().findFirst().get();
        assertThat(pepperKit.getJuniperKitId(), equalTo(kitRequest.getId().toString()));
        assertThat(PepperKitStatus.fromCurrentStatus(pepperKit.getCurrentStatus()), equalTo(PepperKitStatus.RECEIVED));
        assertThat(pepperKit.getLabelDate(), notNullValue());
        assertThat(pepperKit.getScanDate(), notNullValue());
        assertThat(pepperKit.getReceiveDate(), notNullValue());
        assertThat(pepperKit.getTrackingNumber(), notNullValue());
        assertThat(pepperKit.getReturnTrackingNumber(), notNullValue());
    }

    @Autowired
    private EnrolleeFactory enrolleeFactory;
    @Autowired
    private EnvironmentFactory environmentFactory;
    @Autowired
    private KitRequestFactory kitRequestFactory;
    @Autowired
    private StudyEnvironmentFactory studyEnvironmentFactory;
    @Autowired
    private StudyEnvironmentService studyEnvironmentService;
    @Autowired
    private StudyFactory studyFactory;
}
