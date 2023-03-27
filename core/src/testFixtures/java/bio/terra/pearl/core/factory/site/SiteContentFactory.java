package bio.terra.pearl.core.factory.site;

import bio.terra.pearl.core.factory.portal.PortalEnvironmentFactory;
import bio.terra.pearl.core.model.portal.PortalEnvironment;
import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.service.site.SiteContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SiteContentFactory {
    @Autowired
    private PortalEnvironmentFactory portalEnvironmentFactory;
    @Autowired
    private SiteContentService siteContentService;

    public SiteContent.SiteContentBuilder builder(String testName) {
        return SiteContent.builder()
                .defaultLanguage("en");
    }

    public SiteContent.SiteContentBuilder builderWithDependencies(String testName) {
        PortalEnvironment portalEnv = portalEnvironmentFactory.buildPersisted(testName);
        return builder(testName)
                .portalId(portalEnv.getPortalId());
    }

    public SiteContent buildPersisted(String testName) {
        return siteContentService.create(builderWithDependencies(testName).build());
    }
}
