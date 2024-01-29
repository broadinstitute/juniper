package bio.terra.pearl.core.service.consent;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.consent.ConsentFormFactory;
import bio.terra.pearl.core.model.consent.ConsentForm;
import bio.terra.pearl.core.model.consent.StudyEnvironmentConsent;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.PortalParticipantUser;
import bio.terra.pearl.core.model.study.StudyEnvironment;
import bio.terra.pearl.core.model.workflow.ParticipantTask;
import bio.terra.pearl.core.model.workflow.TaskStatus;
import bio.terra.pearl.core.model.workflow.TaskType;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.service.workflow.EnrolleeCreationEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.samePropertyValuesAs;

public class ConsentTaskDispatcherTests extends BaseSpringBootTest {
    @Autowired
    private ConsentTaskDispatcher consentTaskDispatcher;
    @Autowired
    private ConsentFormFactory consentFormFactory;

    @Test
    public void testBuildConsentTasks(TestInfo info) {
        StudyEnvironment studyEnv = StudyEnvironment.builder()
                .id(UUID.randomUUID())
                .build();
        PortalParticipantUser ppUser = PortalParticipantUser.builder()
                .id(UUID.randomUUID())
                .build();
        Enrollee enrollee = Enrollee.builder()
                .participantUserId(UUID.randomUUID())
                .studyEnvironmentId(studyEnv.getId())
                .build();
        EnrolleeCreationEvent enrolleeEvent = EnrolleeCreationEvent.builder()
                .enrollee(enrollee)
                .portalParticipantUser(ppUser)
                .build();
        EnrolleeRuleData enrolleeRuleData = new EnrolleeRuleData(enrollee, null);
        ConsentForm consent = consentFormFactory.builder(getTestName(info)).build();
        StudyEnvironmentConsent studyEnvConsent = StudyEnvironmentConsent.builder()
                .consentForm(consent)
                .eligibilityRule(null)
                .studyEnvironmentId(studyEnv.getId())
                .build();
        List<StudyEnvironmentConsent> studyEnvConsents = Arrays.asList(studyEnvConsent);
        List<ParticipantTask> consentTasks = consentTaskDispatcher
                .buildTasks(enrollee, enrolleeRuleData, ppUser.getId(), studyEnvConsents);

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
                        .status(TaskStatus.NEW)
                        .build(), "createdAt", "lastUpdatedAt"
        ));
    }

    @Test
    public void testMultipleOrderedConsentTasks(TestInfo info) {
        StudyEnvironment studyEnv = StudyEnvironment.builder()
                .id(UUID.randomUUID())
                .build();
        PortalParticipantUser ppUser = PortalParticipantUser.builder()
                .id(UUID.randomUUID())
                .build();
        Enrollee enrollee = Enrollee.builder()
                .participantUserId(UUID.randomUUID())
                .studyEnvironmentId(studyEnv.getId())
                .build();
        EnrolleeCreationEvent enrolleeEvent = EnrolleeCreationEvent.builder()
                .enrollee(enrollee)
                .portalParticipantUser(ppUser)
                .build();
        EnrolleeRuleData enrolleeRuleData = new EnrolleeRuleData(enrollee, null);
        ConsentForm consent1 = consentFormFactory.builder(getTestName(info)).build();
        StudyEnvironmentConsent studyEnvConsent1 = StudyEnvironmentConsent.builder()
                .consentForm(consent1)
                .eligibilityRule(null)
                .consentOrder(1)
                .studyEnvironmentId(studyEnv.getId())
                .build();
        ConsentForm consent2 = consentFormFactory.builder(getTestName(info)).build();
        StudyEnvironmentConsent studyEnvConsent2 = StudyEnvironmentConsent.builder()
                .consentForm(consent2)
                .consentOrder(2)
                .eligibilityRule(null)
                .studyEnvironmentId(studyEnv.getId())
                .build();
        List<StudyEnvironmentConsent> studyEnvConsents = Arrays.asList(studyEnvConsent1, studyEnvConsent2);
        List<ParticipantTask> consentTasks = consentTaskDispatcher
                .buildTasks(enrollee, enrolleeRuleData, ppUser.getId(), studyEnvConsents);
        consentTasks.sort(Comparator.comparing(ParticipantTask::getTaskOrder));
        assertThat(consentTasks, hasSize(2));

        ParticipantTask firstTask = consentTasks.get(0);
        assertThat(firstTask, samePropertyValuesAs(
                ParticipantTask.builder()
                        .blocksHub(true)
                        .studyEnvironmentId(studyEnv.getId())
                        .portalParticipantUserId(ppUser.getId())
                        .enrolleeId(enrollee.getId())
                        .taskOrder(1)
                        .targetName(consent1.getName())
                        .targetStableId(consent1.getStableId())
                        .targetAssignedVersion(consent1.getVersion())
                        .taskType(TaskType.CONSENT)
                        .status(TaskStatus.NEW)
                        .build(), "createdAt", "lastUpdatedAt"
        ));
        ParticipantTask secondTask = consentTasks.get(1);
        assertThat(secondTask, samePropertyValuesAs(
                ParticipantTask.builder()
                        .blocksHub(true)
                        .studyEnvironmentId(studyEnv.getId())
                        .portalParticipantUserId(ppUser.getId())
                        .enrolleeId(enrollee.getId())
                        .taskOrder(2)
                        .targetName(consent2.getName())
                        .targetStableId(consent2.getStableId())
                        .targetAssignedVersion(consent2.getVersion())
                        .taskType(TaskType.CONSENT)
                        .status(TaskStatus.NEW)
                        .build(), "createdAt", "lastUpdatedAt"
        ));
    }
}
