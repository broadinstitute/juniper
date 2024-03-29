package bio.terra.pearl.core.service.publishing;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.model.notification.EmailTemplate;
import bio.terra.pearl.core.model.notification.Trigger;
import bio.terra.pearl.core.model.notification.TriggerEventType;
import bio.terra.pearl.core.model.notification.TriggerType;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.model.publishing.ConfigChange;
import bio.terra.pearl.core.model.publishing.PortalEnvironmentChange;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.survey.Survey;
import bio.terra.pearl.core.model.workflow.TaskType;
import java.util.List;
import java.util.UUID;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class PortalDiffServiceTests extends BaseSpringBootTest {
    @Autowired
    private PortalDiffService portalDiffService;
    @Test
    public void testIsVersionedConfigMatch() {
        // configs match if stableID of template is the same
        Trigger config = Trigger.builder()
                .triggerType(TriggerType.EVENT)
                .eventType(TriggerEventType.STUDY_CONSENT)
                .emailTemplate(EmailTemplate.builder().stableId("foo").build()).build();

        Trigger configWithDifferentTemplate = Trigger.builder()
                .triggerType(TriggerType.EVENT)
                .eventType(TriggerEventType.STUDY_ENROLLMENT)
                .emailTemplate(EmailTemplate.builder().stableId("foo").build()).build();
        assertThat(PortalDiffService.isVersionedConfigMatch(config, configWithDifferentTemplate), equalTo(true));
    }

    @Test
    public void testIsVersionedConfigMatchDifferent() {
        Trigger config = Trigger.builder()
                .triggerType(TriggerType.EVENT)
                .eventType(TriggerEventType.STUDY_CONSENT)
                .emailTemplate(EmailTemplate.builder().stableId("foo").build()).build();

        Trigger configWithDifferentTemplate = Trigger.builder()
                .triggerType(TriggerType.EVENT)
                .eventType(TriggerEventType.STUDY_CONSENT)
                .emailTemplate(EmailTemplate.builder().stableId("bar").build()).build();
        assertThat(PortalDiffService.isVersionedConfigMatch(config, configWithDifferentTemplate), equalTo(false));
    }

    @Test
    public void testDiffNotificationsNoEvents() throws Exception {
        List<Trigger> sourceList = List.of();
        List<Trigger> destList = List.of();
        var diffs = PortalDiffService
                .diffConfigLists(sourceList, destList, PortalDiffService.CONFIG_IGNORE_PROPS);
        assertThat(diffs.addedItems(), hasSize(0));
        assertThat(diffs.changedItems(), hasSize(0));
        assertThat(diffs.removedItems(), hasSize(0));
    }

    @Test
    public void testDiffNotificationsOneEventMatched() throws Exception {
        List<Trigger> sourceList = List.of(
                Trigger.builder().id(UUID.randomUUID())
                        .triggerType(TriggerType.EVENT)
                        .eventType(TriggerEventType.STUDY_CONSENT)
                        .emailTemplate(EmailTemplate.builder().stableId("t1").build())
                        .build());
        List<Trigger> destList = List.of(
                Trigger.builder().id(UUID.randomUUID())
                        .triggerType(TriggerType.EVENT)
                        .eventType(TriggerEventType.STUDY_CONSENT)
                        .emailTemplate(EmailTemplate.builder().stableId("t1").build())
                        .build());
        var diffs = PortalDiffService
                .diffConfigLists(sourceList, destList, PortalDiffService.CONFIG_IGNORE_PROPS);
        assertThat(diffs.addedItems(), hasSize(0));
        assertThat(diffs.changedItems(), hasSize(0));
        assertThat(diffs.removedItems(), hasSize(0));
    }

    @Test
    public void testDiffNotificationsOneEventChanged() throws Exception {
        List<Trigger> sourceList = List.of(
                Trigger.builder().id(UUID.randomUUID())
                        .triggerType(TriggerType.TASK_REMINDER)
                        .taskType(TaskType.CONSENT)
                        .emailTemplate(EmailTemplate.builder().stableId("t1").build())
                        .afterMinutesIncomplete(3000)
                        .build());
        List<Trigger> destList = List.of(
                Trigger.builder().id(UUID.randomUUID())
                        .triggerType(TriggerType.TASK_REMINDER)
                        .taskType(TaskType.CONSENT)
                        .emailTemplate(EmailTemplate.builder().stableId("t1").build())
                        .afterMinutesIncomplete(2000)
                        .build());
        var diffs = PortalDiffService
                .diffConfigLists(sourceList, destList, PortalDiffService.CONFIG_IGNORE_PROPS);
        assertThat(diffs.addedItems(), hasSize(0));
        assertThat(diffs.changedItems(), hasSize(1));
        assertThat(diffs.changedItems().get(0).configChanges(), hasSize(1));
        assertThat(diffs.changedItems().get(0).configChanges().get(0), equalTo(new ConfigChange(
                "afterMinutesIncomplete", 2000, 3000
        )));
        assertThat(diffs.removedItems(), hasSize(0));
    }

    @Test
    public void testDiffNotificationsOneEventAdded() throws Exception {
        Trigger addedConfig = Trigger.builder().id(UUID.randomUUID())
                .triggerType(TriggerType.TASK_REMINDER)
                .taskType(TaskType.CONSENT)
                .afterMinutesIncomplete(3000)
                .build();
        List<Trigger> sourceList = List.of(addedConfig);
        List<Trigger> destList = List.of();
        var diffs = PortalDiffService
                .diffConfigLists(sourceList, destList, PortalDiffService.CONFIG_IGNORE_PROPS);
        assertThat(diffs.addedItems(), hasSize(1));
        assertThat(diffs.addedItems().get(0), samePropertyValuesAs(addedConfig));
        assertThat(diffs.changedItems(), hasSize(0));
        assertThat(diffs.removedItems(), hasSize(0));
    }

    @Test
    public void testDiffNotificationsOneEventRemoved() throws Exception {
        Trigger removedConfig = Trigger.builder().id(UUID.randomUUID())
                .triggerType(TriggerType.TASK_REMINDER)
                .taskType(TaskType.CONSENT)
                .afterMinutesIncomplete(3000)
                .build();
        List<Trigger> sourceList = List.of();
        List<Trigger> destList = List.of(removedConfig);
        var diffs = PortalDiffService
                .diffConfigLists(sourceList, destList, PortalDiffService.CONFIG_IGNORE_PROPS);
        assertThat(diffs.addedItems(), hasSize(0));
        assertThat(diffs.changedItems(), hasSize(0));
        assertThat(diffs.removedItems(), hasSize(1));
        assertThat(diffs.removedItems().get(0), samePropertyValuesAs(removedConfig));
    }


    @Test
    public void testDiffBothUninitialized() throws Exception {
        PortalEnvironment sourceEnv = PortalEnvironment.builder().portalEnvironmentConfig(
                PortalEnvironmentConfig.builder().initialized(false).build()
        ).build();
        PortalEnvironment destEnv = PortalEnvironment.builder().portalEnvironmentConfig(
                PortalEnvironmentConfig.builder().initialized(false).build()
        ).build();
        PortalEnvironmentChange changeRecord = portalDiffService.diffPortalEnvs(sourceEnv, destEnv);
        assertThat(changeRecord.configChanges(), hasSize(0));
        assertThat(changeRecord.siteContentChange().isChanged(), equalTo(false));
        assertThat(changeRecord.preRegSurveyChanges().isChanged(), equalTo(false));
    }

    @Test
    public void testDiffDestUninitialized() throws Exception {
        PortalEnvironment sourceEnv = PortalEnvironment.builder().portalEnvironmentConfig(
                PortalEnvironmentConfig.builder()
                        .emailSourceAddress("blah@blah.com")
                        .initialized(true).build())
                .siteContent(SiteContent.builder().stableId("contentA").version(1).build())
                .preRegSurvey(Survey.builder().stableId("survA").version(1).build())
                .build();
        PortalEnvironment destEnv = PortalEnvironment.builder().portalEnvironmentConfig(
                PortalEnvironmentConfig.builder().initialized(false).build()
        ).build();
        PortalEnvironmentChange changeRecord = portalDiffService.diffPortalEnvs(sourceEnv, destEnv);
        assertThat(changeRecord.configChanges(), hasSize(2));
        assertThat(changeRecord.siteContentChange().isChanged(), equalTo(true));
        assertThat(changeRecord.preRegSurveyChanges().isChanged(), equalTo(true));
    }
}
