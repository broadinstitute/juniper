package bio.terra.pearl.core.service.kit;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.EnvironmentFactory;
import bio.terra.pearl.core.factory.StudyEnvironmentFactory;
import bio.terra.pearl.core.factory.StudyFactory;
import bio.terra.pearl.core.factory.kit.KitRequestFactory;
import bio.terra.pearl.core.factory.participant.EnrolleeFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.study.StudyEnvironmentConfig;
import bio.terra.pearl.core.service.study.StudyEnvironmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasProperty;

class StubPepperDSMClientTest extends BaseSpringBootTest {
    @Autowired
    private StubPepperDSMClient stubPepperDSMClient;

    @Transactional
    @Test
    public void testFetchKitStatus() throws Exception {
        // Arrange
        var study = studyFactory.buildPersisted("testFetchKitStatus");
        environmentFactory.buildPersisted("testFetchKitStatus", EnvironmentName.sandbox);
        var studyEnvironment = studyEnvironmentService.create(
                studyEnvironmentFactory.builder("testFetchKitStatus")
                        .studyId(study.getId())
                        .environmentName(EnvironmentName.sandbox)
                        .studyEnvironmentConfig(new StudyEnvironmentConfig())
                        .build());
        var enrollee = enrolleeFactory.buildPersisted("testFetchKitStatus", studyEnvironment);
        var kit = kitRequestFactory.buildPersisted("testFetchKitStatus", enrollee.getId());

        // Act
        var kitStatus = stubPepperDSMClient.fetchKitStatus(kit.getId());

        // Assert
        assertThat(kitStatus, hasProperty("currentStatus", equalTo("SENT")));
    }

    @Transactional
    @Test
    public void testFetchKitStatusByStudy() throws Exception {
        // Arrange
        var study = studyFactory.buildPersisted("testFetchKitStatusByStudy");
        environmentFactory.buildPersisted("testFetchKitStatusByStudy", EnvironmentName.sandbox);
        var studyEnvironment = studyEnvironmentService.create(
                studyEnvironmentFactory.builder("testFetchKitStatusByStudy")
                        .studyId(study.getId())
                        .environmentName(EnvironmentName.sandbox)
                        .studyEnvironmentConfig(new StudyEnvironmentConfig())
                        .build());
        var enrollee = enrolleeFactory.buildPersisted("testFetchKitStatusByStudy", studyEnvironment);
        var kit = kitRequestFactory.buildPersisted("testFetchKitStatusByStudy", enrollee.getId());

        // Act
        var kitStatuses = stubPepperDSMClient.fetchKitStatusByStudy(study.getShortcode());

        // Assert
        assertThat(kitStatuses.size(), equalTo(1));
        var kitStatus = kitStatuses.stream().findFirst().get();
        assertThat(kitStatus.getJuniperKitId(), equalTo(kit.getId().toString()));
        assertThat(kitStatus.getCurrentStatus(), equalTo("SENT"));
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
