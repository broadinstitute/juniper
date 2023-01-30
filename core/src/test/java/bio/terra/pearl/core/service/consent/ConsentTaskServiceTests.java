package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.participant.*;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.study.EnrolleeCreationEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.springframework.beans.factory.annotation.Autowired;

public class ConsentTaskServiceTests extends BaseSpringBootTest {
    @Autowired
    private ConsentTaskService consentTaskService;
    @Test
    public void testBuildConsentTasks() {
        StudyEnvironment studyEnv = StudyEnvironment.builder()
                .id(UUID.randomUUID())
                .build();
        PortalParticipantUser ppUser = PortalParticipantUser.builder()
                .id(UUID.randomUUID())
                .build();
        Enrollee enrollee = Enrollee.builder()
                .participantUserId(UUID.randomUUID())
                .build();
        EnrolleeCreationEvent enrolleeEvent = EnrolleeCreationEvent.builder()
                .enrollee(enrollee)
                .portalParticipantUser(ppUser)
                .studyEnvironment(studyEnv)
                .build();
        List<ParticipantTask> allTasks = new ArrayList<>();
        EnrolleeRuleData enrolleeRuleData = EnrolleeRuleData.builder()
                .enrollee(enrollee)
                .build();
        ConsentForm consent = ConsentForm.builder()
                .id(UUID.randomUUID())
                .name("Test consent 1")
                .stableId("testConsent1")
                .version(2)
                .build();
        StudyEnvironmentConsent studyEnvConsent = StudyEnvironmentConsent.builder()
                .consentForm(consent)
                .eligibilityRule(null)
                .studyEnvironmentId(studyEnv.getId())
                .build();
        List<StudyEnvironmentConsent> studyEnvConsents = Arrays.asList(studyEnvConsent);
        List<ParticipantTask> consentTasks = consentTaskService
                .buildConsentTasks(enrolleeEvent, allTasks, enrolleeRuleData, studyEnvConsents);

        assertThat(consentTasks, hasSize(1));
        ParticipantTask newTask = consentTasks.get(0);
        assertThat(newTask, samePropertyValuesAs(
                ParticipantTask.builder()
                        .blocksHub(true)
                        .studyEnvironmentId(studyEnv.getId())
                        .portalParticipantUserId(ppUser.getId())
                        .enrolleeId(enrollee.getId())
                        .taskOrder(0)
                        .targetName("Test consent 1")
                        .targetStableId("testConsent1")
                        .targetAssignedVersion(2)
                        .taskType(TaskType.CONSENT)
                        .status(TaskStatus.NEW.name())
                        .build(), "createdAt", "lastUpdatedAt"
        ));


    }
}
