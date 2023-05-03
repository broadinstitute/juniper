package bio.terra.pearl.core.service.notification.email;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.Enrollee;
import bio.terra.pearl.core.model.participant.Profile;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.service.notification.NotificationContextInfo;
import bio.terra.pearl.core.service.notification.substitutors.EnrolleeEmailSubstitutor;
import bio.terra.pearl.core.service.rule.EnrolleeRuleData;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import org.apache.commons.text.StringSubstitutor;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;


public class EnrolleeEmailSubstitutorTests extends BaseSpringBootTest {
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private ApplicationRoutingPaths routingPaths;

    @Test
    public void profileVariablesAreReplaced() {
        Profile profile = Profile.builder().givenName("tester").build();
        Enrollee enrollee = Enrollee.builder().build();
        EnrolleeRuleData ruleData = new EnrolleeRuleData(enrollee, profile);

        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder().build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder("profileVariablesAreReplaced")
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();
        Portal portal = Portal.builder().build();
        var contextInfo = new NotificationContextInfo(portal, portalEnv, null, null);
        StringSubstitutor replacer = EnrolleeEmailSubstitutor.newSubstitutor(ruleData, contextInfo, routingPaths);
        assertThat(replacer.replace("name is ${profile.givenName}"), equalTo("name is tester"));
    }

    @Test
    public void envConfigVariablesAreReplaced() {
        Profile profile = Profile.builder().build();
        Enrollee enrollee = Enrollee.builder().build();
        EnrolleeRuleData ruleData = new EnrolleeRuleData(enrollee, profile);
        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname("testHostName")
                .build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder("envConfigVariablesAreReplaced")
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();
        Portal portal = Portal.builder().build();
        var contextInfo = new NotificationContextInfo(portal, portalEnv, null, null);
        StringSubstitutor replacer = EnrolleeEmailSubstitutor.newSubstitutor(ruleData, contextInfo, routingPaths);
        assertThat(replacer.replace("host is ${envConfig.participantHostname}"),
                equalTo("host is testHostName"));
    }

    @Test
    public void studyNameVariablesAreReplaced() {
        Profile profile = Profile.builder().build();
        Enrollee enrollee = Enrollee.builder().build();
        EnrolleeRuleData ruleData = new EnrolleeRuleData(enrollee, profile);
        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname("testHostName")
                .build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder("envConfigVariablesAreReplaced")
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();
        Study study = Study.builder().name("testStudyName").build();
        Portal portal = Portal.builder().build();
        var contextInfo = new NotificationContextInfo(portal, portalEnv, study, null);
        StringSubstitutor replacer = EnrolleeEmailSubstitutor.newSubstitutor(ruleData, contextInfo, routingPaths);
        assertThat(replacer.replace("welcome to ${study.name}"),
                equalTo("welcome to testStudyName"));
    }

    @Test
    public void testDashLinkVariablesReplaced() {
        Profile profile = Profile.builder().build();
        Enrollee enrollee = Enrollee.builder().build();
        EnrolleeRuleData ruleData = new EnrolleeRuleData(enrollee, profile);
        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname("newstudy.org")
                .build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder("testDashLinkVariablesReplaced")
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();
        Portal portal = Portal.builder().name("PortalA").build();

        var contextInfo = new NotificationContextInfo(portal, portalEnv, null, null);
        StringSubstitutor replacer = EnrolleeEmailSubstitutor.newSubstitutor(ruleData, contextInfo, routingPaths);
        assertThat(replacer.replace("here's a dashboard link: ${dashboardLink}"),
                equalTo("here's a dashboard link: <a href=\"https://irb.newstudy.org/hub\">Return to PortalA</a>"));


        portalEnv.setEnvironmentName(EnvironmentName.live);
        StringSubstitutor replacerLive = EnrolleeEmailSubstitutor.newSubstitutor(ruleData, contextInfo, routingPaths);
        assertThat(replacerLive.replace("here's a dashboard link: ${dashboardLink}"),
                equalTo("here's a dashboard link: <a href=\"https://newstudy.org/hub\">Return to PortalA</a>"));

    }

    @Test
    public void testImageVariablesReplaced() {
        Profile profile = Profile.builder().build();
        Enrollee enrollee = Enrollee.builder().build();
        EnrolleeRuleData ruleData = new EnrolleeRuleData(enrollee, profile);
        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname("newstudy.org")
                .build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder("testImageVariablesReplaced")
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();
        Portal portal = Portal.builder().name("PortalA").shortcode("foo").build();

        var contextInfo = new NotificationContextInfo(portal, portalEnv, null, null);
        StringSubstitutor replacer = EnrolleeEmailSubstitutor.newSubstitutor(ruleData, contextInfo, routingPaths);
        assertThat(replacer.replace("here's an image: <img src=\"${siteImageBaseUrl}/1/ourhealth-logo.png\"/>"),
                equalTo("here's an image: <img src=\"https://irb.newstudy.org/api/public/portals/v1/foo/env/irb/siteImages/1/ourhealth-logo.png\"/>"));
    }
}
