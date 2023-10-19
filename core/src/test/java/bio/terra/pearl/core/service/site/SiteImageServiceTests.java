package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.factory.site.SiteImageFactory;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.site.SiteImage;
import bio.terra.pearl.core.model.site.SiteImageMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class SiteImageServiceTests extends BaseSpringBootTest {
    @Autowired
    private SiteImageService siteImageService;

    @Autowired
    private SiteImageFactory siteImageFactory;
    @Autowired
    private PortalFactory portalFactory;

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
    public void testCrud(TestInfo testInfo) {
        SiteImage image = siteImageFactory.builderWithDependencies("testSiteImageCrud").build();
        SiteImage savedImage = siteImageService.create(image);
        Assertions.assertNotNull(savedImage.getId());
        SiteImage imageByShortCode = siteImageService.findOne(savedImage.getPortalShortcode(),
                savedImage.getCleanFileName(), savedImage.getVersion()  ).get();
        Assertions.assertEquals(savedImage.getId(), imageByShortCode.getId());
    }

    @Test
    @Transactional
    public void testFindMetadataByPortal(TestInfo testInfo) {
        SiteImage image = siteImageFactory.builderWithDependencies("testFindMetadataByPortal")
                .data("imageData".getBytes()).build();
        SiteImage savedImage = siteImageService.create(image);
        Portal emptyPortal = portalFactory.buildPersisted(getTestName(testInfo));
        assertThat(siteImageService.findMetadataByPortal(emptyPortal.getShortcode()),
                hasSize(0));
        List<SiteImageMetadata> imageList = siteImageService.findMetadataByPortal((image.getPortalShortcode()));
        assertThat(imageList, hasSize(1));
        assertThat(imageList.get(0).getCleanFileName(), equalTo(savedImage.getCleanFileName()));
    }

    @Test
    @Transactional
    public void testAddsCleanFileName() {
        SiteImage image = siteImageFactory.builderWithDependencies("testSiteImageAddsCleanFileName")
                .cleanFileName(null)
                .build();
        SiteImage savedImage = siteImageService.create(image);
        assertThat(savedImage.getCleanFileName(), equalTo(SiteImageService.cleanFileName(image.getUploadFileName())));
    }

    @Test
    @Transactional
    public void testSanitizesCleanFileName() {
        String dirtyFileName = "testSanitizesCleanFileName with spaces.png";
        SiteImage image = siteImageFactory.builderWithDependencies("testSanitizesCleanFileName")
                .cleanFileName(dirtyFileName)
                .build();
        SiteImage savedImage = siteImageService.create(image);
        assertThat(savedImage.getCleanFileName(), equalTo( SiteImageService.cleanFileName(dirtyFileName)));
    }
}
