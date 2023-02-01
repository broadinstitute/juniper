package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.consent.ConsentFormFactory;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.participant.*;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.workflow.EnrolleeCreationEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class ConsentTaskDispatcherTests extends BaseSpringBootTest {
    @Autowired
    private ConsentTaskDispatcher consentTaskDispatcher;
    @Autowired
    private ConsentFormFactory consentFormFactory;

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
        ConsentForm consent = consentFormFactory.builder("testBuildConsentTasks").build();
        StudyEnvironmentConsent studyEnvConsent = StudyEnvironmentConsent.builder()
                .consentForm(consent)
                .eligibilityRule(null)
                .studyEnvironmentId(studyEnv.getId())
                .build();
        List<StudyEnvironmentConsent> studyEnvConsents = Arrays.asList(studyEnvConsent);
        List<ParticipantTask> consentTasks = consentTaskDispatcher
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
                        .targetName(consent.getName())
                        .targetStableId(consent.getStableId())
                        .targetAssignedVersion(consent.getVersion())
                        .taskType(TaskType.CONSENT)
                        .status(TaskStatus.NEW.name())
                        .build(), "createdAt", "lastUpdatedAt"
        ));
    }
}
