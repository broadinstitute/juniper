package bio.terra.pearl.core.dao.portal;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.EnvironmentFactory;
import bio.terra.pearl.core.model.EnvironmentName;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.portal.PortalEnvironmentConfig;
import bio.terra.pearl.core.service.portal.PortalEnvironmentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class PortalDaoTests extends BaseSpringBootTest {
    @Autowired
    private PortalDao portalDao;
    @Autowired
    private PortalEnvironmentService portalEnvironmentService;
    @Autowired
    private EnvironmentFactory environmentFactory;

    @Test
    @Transactional
    public void testFindByShortcodeOrHostname(TestInfo info) {
        environmentFactory.buildPersisted(getTestName(info), EnvironmentName.sandbox);
        environmentFactory.buildPersisted(getTestName(info), EnvironmentName.live);
        Portal fooPortal = portalDao.create(Portal.builder().shortcode("foo").name("fooName").build());
        Portal barPortal =  portalDao.create(Portal.builder().shortcode("bar").name("barName").build());

        assertThat(portalDao.findOneByShortcodeOrHostname("foo").get().getName(), equalTo("fooName"));
        assertThat(portalDao.findOneByShortcodeOrHostname("blah").isPresent(), equalTo(false));

        PortalEnvironmentConfig fooSandboxConfig = PortalEnvironmentConfig.builder()
                .participantHostname("customdomain.org").build();
        PortalEnvironment fooSandbox =  portalEnvironmentService.create(PortalEnvironment.builder()
                .environmentName(EnvironmentName.sandbox)
                .portalEnvironmentConfig(fooSandboxConfig)
                .portalId(fooPortal.getId()).build());

        // confirm we can retrieve by the domain
        assertThat(portalDao.findOneByShortcodeOrHostname("customdomain").get().getName(), equalTo("fooName"));

        // confirm regular matches aren't impacted
        assertThat(portalDao.findOneByShortcodeOrHostname("foo").get().getName(), equalTo("fooName"));
        assertThat(portalDao.findOneByShortcodeOrHostname("bar").get().getName(), equalTo("barName"));

        // confirm partial matches aren't good enough
        assertThat(portalDao.findOneByShortcodeOrHostname("customdo").isPresent(), equalTo(false));
        assertThat(portalDao.findOneByShortcodeOrHostname("customdomain.com").isPresent(), equalTo(false));


    }
}
