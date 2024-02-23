package bio.terra.pearl.core.factory.site;

import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.site.SiteMedia;
import bio.terra.pearl.core.service.site.SiteMediaService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SiteMediaFactory {
    @Autowired
    private PortalFactory portalFactory;
    @Autowired
    private SiteMediaService siteMediaService;

    public SiteMedia.SiteMediaBuilder<?, ?> builder(String testName) {
        String filename = testName + RandomStringUtils.randomAlphabetic(3) + ".png";
        return SiteMedia.builder().data("abc123".getBytes())
                .version(1)
                .uploadFileName(filename)
                .cleanFileName(filename);
    }

    public SiteMedia.SiteMediaBuilder<?, ?> builderWithDependencies(String testName) {
        Portal portal = portalFactory.buildPersisted(testName);
        return builder(testName)
                .portalShortcode(portal.getShortcode());
    }

    public SiteMedia buildPersisted(String testName) {
        SiteMedia image = builderWithDependencies(testName).build();
        return siteMediaService.create(image);
    }
}
