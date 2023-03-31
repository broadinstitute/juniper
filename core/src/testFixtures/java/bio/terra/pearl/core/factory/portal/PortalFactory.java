package bio.terra.pearl.core.factory.portal;

import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.service.portal.PortalService;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PortalFactory {
    @Autowired
    private PortalService portalService;

    public Portal.PortalBuilder builder(String testName) {
        return Portal.builder()
                .name(testName + RandomStringUtils.randomAlphabetic(6))
                .shortcode(RandomStringUtils.randomAlphabetic(7));
    }

    public Portal.PortalBuilder builderWithDependencies(String testName) {
        return builder(testName);
    }

    public Portal buildPersisted(String testName) {
        return portalService.create(builderWithDependencies(testName).build());
    }
}
