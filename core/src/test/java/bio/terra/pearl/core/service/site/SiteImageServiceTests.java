package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.site.SiteImageFactory;
import bio.terra.pearl.core.model.site.SiteImage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class SiteImageServiceTests extends BaseSpringBootTest {
    @Autowired
    private SiteImageService siteImageService;

    @Autowired
    private SiteImageFactory siteImageFactory;

    @Test
    public void testGenerateCleanFileName() {
        String shortcode = SiteImageService
                .cleanFileName("foo.gif");
        Assertions.assertEquals("foo.gif", shortcode);
    }

    @Test
    public void testGenerateSpecialCharShortcode() {
        String shortcode = SiteImageService
                .cleanFileName("spaces Capitals (1).gif");
        Assertions.assertEquals("spaces_capitals_1.gif", shortcode);
    }

    @Test
    @Transactional
    public void testCrud() {
        SiteImage image = siteImageFactory.builderWithDependencies("testSiteImageCrud").build();
        SiteImage savedImage = siteImageService.create(image);
        Assertions.assertNotNull(savedImage.getId());
        SiteImage imageByShortCode = siteImageService.findOne(savedImage.getPortalShortcode(),
                savedImage.getCleanFileName(), savedImage.getVersion()  ).get();
        Assertions.assertEquals(savedImage.getId(), imageByShortCode.getId());
    }
}
