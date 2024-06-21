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
    public void testDiffBothUninitialized() throws Exception {
        PortalEnvironment sourceEnv = PortalEnvironment.builder().portalEnvironmentConfig(
                PortalEnvironmentConfig.builder().initialized(false).build()
        ).build();
        PortalEnvironment destEnv = PortalEnvironment.builder().portalEnvironmentConfig(
                PortalEnvironmentConfig.builder().initialized(false).build()
        ).build();
        PortalEnvironmentChange changeRecord = portalDiffService.diffPortalEnvs(sourceEnv, destEnv);
        assertThat(changeRecord.getConfigChanges(), hasSize(0));
        assertThat(changeRecord.getSiteContentChange().isChanged(), equalTo(false));
        assertThat(changeRecord.getPreRegSurveyChanges().isChanged(), equalTo(false));
    }

    @Test
    public void testDiffDestUninitialized() throws Exception {
        PortalEnvironment sourceEnv = PortalEnvironment.builder().portalEnvironmentConfig(
                PortalEnvironmentConfig.builder()
                        .emailSourceAddress("blah@blah.com")
                        .initialized(true).build())
                .siteContent(SiteContent.builder().stableId("contentA").version(1).build())
                .preRegSurvey(Survey.builder().stableId("survA").version(1).build())
                .triggers(List.of(Trigger.builder().triggerType(TriggerType.EVENT)
                        .eventType(TriggerEventType.STUDY_CONSENT)
                        .emailTemplate(EmailTemplate.builder().stableId("foo").build()).build()))
                .build();
        PortalEnvironment destEnv = PortalEnvironment.builder().portalEnvironmentConfig(
                PortalEnvironmentConfig.builder().initialized(false).build()
        ).build();
        PortalEnvironmentChange changeRecord = portalDiffService.diffPortalEnvs(sourceEnv, destEnv);
        assertThat(changeRecord.getConfigChanges(), hasSize(2));
        assertThat(changeRecord.getSiteContentChange().isChanged(), equalTo(true));
        assertThat(changeRecord.getPreRegSurveyChanges().isChanged(), equalTo(true));
        assertThat(changeRecord.getTriggerChanges().addedItems(), hasSize(1));
    }
}
