package bio.terra.pearl.core.service.notification.email;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.participant.*;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.model.study.Study;
import bio.terra.pearl.core.service.notification.NotificationContextInfo;
import bio.terra.pearl.core.service.notification.substitutors.EnrolleeEmailSubstitutor;
import bio.terra.pearl.core.service.rule.EnrolleeContext;
import bio.terra.pearl.core.shared.ApplicationRoutingPaths;
import org.apache.commons.text.StringSubstitutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;


public class EnrolleeEmailSubstitutorTests extends BaseSpringBootTest {
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private ApplicationRoutingPaths routingPaths;

    @Test
    public void profileVariablesAreReplaced() {
        Profile profile = Profile.builder().givenName("tester").build();
        Enrollee enrollee = Enrollee.builder().build();
        EnrolleeContext ruleData = new EnrolleeContext(enrollee, profile, null, null);

        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder().build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder("profileVariablesAreReplaced")
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();
        Portal portal = Portal.builder().build();
        NotificationContextInfo contextInfo = new NotificationContextInfo(portal, portalEnv, portalEnvironmentConfig, null, null);
        StringSubstitutor replacer = EnrolleeEmailSubstitutor.newSubstitutor(ruleData, contextInfo, routingPaths);
        assertThat(replacer.replace("name is ${profile.givenName}"), equalTo("name is tester"));
    }

    @Test
    public void envConfigVariablesAreReplaced() {
        EnrolleeContext ruleData = new EnrolleeContext(new Enrollee(), new Profile(), null, null);
        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname("testHostName")
                .build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder("envConfigVariablesAreReplaced")
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();
        Portal portal = Portal.builder().build();
        NotificationContextInfo contextInfo = new NotificationContextInfo(portal, portalEnv, portalEnvironmentConfig, null, null);
        StringSubstitutor replacer = EnrolleeEmailSubstitutor.newSubstitutor(ruleData, contextInfo, routingPaths);
        assertThat(replacer.replace("host is ${envConfig.participantHostname}"),
                equalTo("host is testHostName"));
    }

    @Test
    public void studyNameVariablesAreReplaced(TestInfo info) {
        EnrolleeContext ruleData = new EnrolleeContext(new Enrollee(), new Profile(), null, null);
        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname("testHostName")
                .build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder("envConfigVariablesAreReplaced")
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();
        Study study = Study.builder().name(getTestName(info)).build();
        Portal portal = Portal.builder().build();
        NotificationContextInfo contextInfo = new NotificationContextInfo(portal, portalEnv, portalEnvironmentConfig, study, null);
        StringSubstitutor replacer = EnrolleeEmailSubstitutor.newSubstitutor(ruleData, contextInfo, routingPaths);
        assertThat(replacer.replace("welcome to ${study.name}"),
                equalTo("welcome to " + getTestName(info)));
    }

    @Test
    public void testDashLinkVariablesReplaced(TestInfo info) {
        EnrolleeContext ruleData = new EnrolleeContext(new Enrollee(), new Profile(), null, null);
        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname("newstudy.org")
                .build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder(getTestName(info))
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();
        Portal portal = Portal.builder().name("PortalA").build();

        NotificationContextInfo contextInfo = new NotificationContextInfo(portal, portalEnv, portalEnvironmentConfig, null, null);
        StringSubstitutor replacer = EnrolleeEmailSubstitutor.newSubstitutor(ruleData, contextInfo, routingPaths);
        assertThat(replacer.replace("here's a dashboard link: ${dashboardLink}"),
                equalTo("here's a dashboard link: <a href=\"https://irb.newstudy.org/hub\">Return to PortalA</a>"));


        portalEnv.setEnvironmentName(EnvironmentName.live);
        StringSubstitutor replacerLive = EnrolleeEmailSubstitutor.newSubstitutor(ruleData, contextInfo, routingPaths);
        assertThat(replacerLive.replace("here's a dashboard link: ${dashboardLink}"),
                equalTo("here's a dashboard link: <a href=\"https://newstudy.org/hub\">Return to PortalA</a>"));

    }

    @Test
    public void testMailLinkVariablesReplaced(TestInfo info) {
        EnrolleeContext ruleData = new EnrolleeContext(new Enrollee(), new Profile(), null, null);
        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .emailSourceAddress("info@test.edu")
                .build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder(getTestName(info))
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();
        Portal portal = Portal.builder().name("PortalA").build();

        NotificationContextInfo contextInfo = new NotificationContextInfo(portal, portalEnv, portalEnvironmentConfig, null, null);
        StringSubstitutor replacer = EnrolleeEmailSubstitutor.newSubstitutor(ruleData, contextInfo, routingPaths);
        assertThat(replacer.replace("Contact us at: ${participantSupportEmailLink}"),
                equalTo("Contact us at: <a href=\"mailto:info@test.edu\" rel=\"noopener\" target=\"_blank\">info@test.edu</a>"));
    }

    @Test
    public void testImageVariablesReplaced(TestInfo info) {
        EnrolleeContext ruleData = new EnrolleeContext(new Enrollee(), new Profile(), null, null);
        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname("newstudy.org")
                .build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder(getTestName(info))
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();
        Portal portal = Portal.builder().name("PortalA").shortcode("foo").build();

        NotificationContextInfo contextInfo = new NotificationContextInfo(portal, portalEnv, portalEnvironmentConfig, null, null);
        StringSubstitutor replacer = EnrolleeEmailSubstitutor.newSubstitutor(ruleData, contextInfo, routingPaths);
        assertThat(replacer.replace("here's an image: <img src=\"${siteMediaBaseUrl}/1/ourhealth-logo.png\"/>"),
                equalTo("here's an image: <img src=\"https://irb.newstudy.org/api/public/portals/v1/foo/env/irb/siteMedia/1/ourhealth-logo.png\"/>"));
    }

    @Test
    public void testParticipantUserVariablesReplaced(TestInfo info) {
        ParticipantUser user = ParticipantUser.builder().username("test123@test.com").build();
        EnrolleeContext ruleData = new EnrolleeContext(new Enrollee(), new Profile(), user, null);
        PortalEnvironment portalEnv = PortalEnvironment.builder().environmentName(EnvironmentName.irb).build();
        NotificationContextInfo contextInfo = new NotificationContextInfo(new Portal(), portalEnv, new PortalEnvironmentConfig(), null, null);
        StringSubstitutor replacer = EnrolleeEmailSubstitutor.newSubstitutor(ruleData, contextInfo, routingPaths);
        assertThat(replacer.replace("your username is ${participantUser.username}"),
                equalTo("your username is test123@test.com"));
    }

    @Test
    public void testInvitationLinkReplaced(TestInfo info) {
        EnrolleeContext ruleData = new EnrolleeContext(new Enrollee(), new Profile(), ParticipantUser.builder().username("test123@test.com").build(), null);
        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname("newstudy.org")
                .build();
        PortalEnvironment portalEnv = portalEnvironmentFactory.builder(getTestName(info))
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();
        Portal portal = Portal.builder().name("PortalA").build();

        NotificationContextInfo contextInfo = new NotificationContextInfo(portal, portalEnv, portalEnvironmentConfig, null, null);
        StringSubstitutor replacer = EnrolleeEmailSubstitutor.newSubstitutor(ruleData, contextInfo, routingPaths);
        assertThat(replacer.replace("here's an invite: ${invitationLink}"),
                equalTo("here's an invite: https://irb.newstudy.org/join/invitation?accountName=test123%40test.com&preferredLanguage=en"));
    }

    @Test
    public void testProxyTernary(TestInfo info) {
        UUID enrolleeId = UUID.randomUUID();
        EnrolleeContext proxyData = new EnrolleeContext(
                Enrollee.builder().id(enrolleeId).build(),
                Profile.builder().givenName("test name").build(),
                ParticipantUser.builder().username("asdf@asdf.com").build(),
                List.of(
                        // enrollee has a proxy
                        EnrolleeRelation
                                .builder()
                                .targetEnrolleeId(enrolleeId)
                                .relationshipType(RelationshipType.PROXY)
                                .build()
                ));

        EnrolleeContext nonProxyData = new EnrolleeContext(
                Enrollee.builder().id(enrolleeId).build(),
                Profile.builder().givenName("test name").build(),
                ParticipantUser.builder().username("asdf@asdf.com").build(),
                null);

        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname("newstudy.org")
                .build();

        PortalEnvironment portalEnv = portalEnvironmentFactory.builder(getTestName(info))
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();

        Portal portal = Portal.builder().name("PortalA").build();

        NotificationContextInfo contextInfo = new NotificationContextInfo(portal, portalEnv, portalEnvironmentConfig, null, null);

        StringSubstitutor proxyReplacer = EnrolleeEmailSubstitutor.newSubstitutor(proxyData, contextInfo, routingPaths);
        StringSubstitutor nonProxyReplacer = EnrolleeEmailSubstitutor.newSubstitutor(nonProxyData, contextInfo, routingPaths);

        assertThat(proxyReplacer.replace("${isProxy ? \"Proxy is true\" : \"Proxy is false\"}"),
                equalTo("Proxy is true"));
        assertThat(nonProxyReplacer.replace("${isProxy ? \"Proxy is true\" : \"Proxy is false\"}"),
                equalTo("Proxy is false"));

        assertThat(proxyReplacer.replace("${isProxy ? profile.givenName : \"Proxy is false\"}"),
                equalTo("test name"));
        assertThat(nonProxyReplacer.replace("${isProxy ? \"Proxy is true\" : profile.givenName}"),
                equalTo("test name"));

    }

    @Test
    public void testProxyInvitationLink(TestInfo info) {
        UUID enrolleeId = UUID.randomUUID();
        EnrolleeContext proxyData = new EnrolleeContext(
                Enrollee.builder().id(enrolleeId).build(),
                Profile.builder().givenName("test name").build(),
                ParticipantUser.builder().username("asdf@asdf.com-prox-TEST").build(),
                List.of(
                        // enrollee has a proxy
                        EnrolleeRelation
                                .builder()
                                .targetEnrolleeId(enrolleeId)
                                .relationshipType(RelationshipType.PROXY)
                                .build()
                ));

        EnrolleeContext nonProxyData = new EnrolleeContext(
                Enrollee.builder().id(enrolleeId).build(),
                Profile.builder().givenName("test name").build(),
                ParticipantUser.builder().username("asdf@asdf.com").build(),
                null);

        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname("newstudy.org")
                .build();

        PortalEnvironment portalEnv = portalEnvironmentFactory.builder(getTestName(info))
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();

        Portal portal = Portal.builder().name("PortalA").build();

        NotificationContextInfo contextInfo = new NotificationContextInfo(portal, portalEnv, portalEnvironmentConfig, null, null);

        StringSubstitutor proxyReplacer = EnrolleeEmailSubstitutor.newSubstitutor(proxyData, contextInfo, routingPaths);
        StringSubstitutor nonProxyReplacer = EnrolleeEmailSubstitutor.newSubstitutor(nonProxyData, contextInfo, routingPaths);

        assertThat(proxyReplacer.replace("here's an invite: ${invitationLink}"),
                equalTo("here's an invite: https://irb.newstudy.org/join/invitation?accountName=asdf%40asdf.com&preferredLanguage=en"));
        assertThat(nonProxyReplacer.replace("here's an invite: ${invitationLink}"),
                equalTo("here's an invite: https://irb.newstudy.org/join/invitation?accountName=asdf%40asdf.com&preferredLanguage=en"));
    }

    @Test
    public void testInvitationLinkSpecifiesLanguage(TestInfo info) {
        UUID enrolleeId = UUID.randomUUID();
        EnrolleeContext esData = new EnrolleeContext(
                Enrollee.builder().id(enrolleeId).build(),
                Profile.builder().preferredLanguage("es").build(),
                ParticipantUser.builder().username("asdf@asdf.com").build(),
                null);

        EnrolleeContext zhData = new EnrolleeContext(
                Enrollee.builder().id(enrolleeId).build(),
                Profile.builder().givenName("test name").preferredLanguage("zh").build(),
                ParticipantUser.builder().username("asdf@asdf.com").build(),
                null);

        PortalEnvironmentConfig portalEnvironmentConfig = PortalEnvironmentConfig.builder()
                .participantHostname("newstudy.org")
                .build();

        PortalEnvironment portalEnv = portalEnvironmentFactory.builder(getTestName(info))
                .portalEnvironmentConfig(portalEnvironmentConfig).environmentName(EnvironmentName.irb).build();

        Portal portal = Portal.builder().name("PortalA").build();

        NotificationContextInfo contextInfo = new NotificationContextInfo(portal, portalEnv, portalEnvironmentConfig, null, null);

        StringSubstitutor esReplacer = EnrolleeEmailSubstitutor.newSubstitutor(esData, contextInfo, routingPaths);
        StringSubstitutor zhReplacer = EnrolleeEmailSubstitutor.newSubstitutor(zhData, contextInfo, routingPaths);

        assertThat(esReplacer.replace("here's an invite: ${invitationLink}"),
                equalTo("here's an invite: https://irb.newstudy.org/join/invitation?accountName=asdf%40asdf.com&preferredLanguage=es"));
        assertThat(zhReplacer.replace("here's an invite: ${invitationLink}"),
                equalTo("here's an invite: https://irb.newstudy.org/join/invitation?accountName=asdf%40asdf.com&preferredLanguage=zh"));
    }
}
