package bio.terra.pearl.core.service.notification;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import org.apache.commons.text.StringSubstitutor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class EnrolleeEmailSubstitutorTests extends BaseSpringBootTest {
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;

    @Test
    public void profileVariablesAreReplaced() {
        Profile profile = Profile.builder().givenName("tester").build();
        Enrollee enrollee = Enrollee.builder().build();
        EnrolleeRuleData ruleData = EnrolleeRuleData.builder().enrollee(enrollee).profile(profile).build();

        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder().build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder("profileVariablesAreReplaced")
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();

        StringSubstitutor replacer = EnrolleeEmailSubstitutor.newSubstitutor(ruleData, portalEnv, "test");
        assertThat(replacer.replace("name is ${profile.givenName}"), equalTo("name is tester"));
    }

    @Test
    public void envConfigVariablesAreReplaced() {
        Profile profile = Profile.builder().build();
        Enrollee enrollee = Enrollee.builder().build();
        EnrolleeRuleData ruleData = EnrolleeRuleData.builder().enrollee(enrollee).profile(profile).build();
        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname("testHostName")
                .build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder("envConfigVariablesAreReplaced")
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();

        StringSubstitutor replacer = EnrolleeEmailSubstitutor.newSubstitutor(ruleData, portalEnv, "test");
        assertThat(replacer.replace("host is ${envConfig.participantHostname}"),
                equalTo("host is testHostName"));
    }

    @Test
    public void testDashLinkVariablesReplaced() {
        Profile profile = Profile.builder().build();
        Enrollee enrollee = Enrollee.builder().build();
        EnrolleeRuleData ruleData = EnrolleeRuleData.builder().enrollee(enrollee).profile(profile).build();
        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname("newstudy.org")
                .build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder("testDashLinkVariablesReplaced")
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();

        StringSubstitutor replacer = EnrolleeEmailSubstitutor.newSubstitutor(ruleData, portalEnv, "test");
        assertThat(replacer.replace("here's a dashboard link: ${dashboardLink}"),
                equalTo("here's a dashboard link: <a href=\"https://newstudy.org/hub\">Go to dashboard</a>"));
    }
}
