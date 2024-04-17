package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.portal.PortalFactory;
import bio.terra.pearl.core.factory.site.SiteMediaFactory;
import bio.terra.pearl.core.model.portal.Portal;
import bio.terra.pearl.core.model.site.SiteMedia;
import bio.terra.pearl.core.model.site.SiteMediaMetadata;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class SiteMediaServiceTests extends BaseSpringBootTest {
    @Autowired
    private SiteMediaService siteMediaService;

    @Autowired
    private SiteMediaFactory siteMediaFactory;
    @Autowired
    private PortalFactory portalFactory;

    @Test
    public void testGenerateCleanFileName() {
        String shortcode = SiteMediaService
                .cleanFileName("foo.gif");
        Assertions.assertEquals("foo.gif", shortcode);
    }

    @Test
    public void testGenerateSpecialCharShortcode() {
        String shortcode = SiteMediaService
                .cleanFileName("spaces Capitals (1).gif");
        Assertions.assertEquals("spaces_capitals_1.gif", shortcode);
    }

    @Test
    @Transactional
    public void testCrud(TestInfo testInfo) {
        SiteMedia image = siteMediaFactory.builderWithDependencies(getTestName(testInfo)).build();
        SiteMedia savedImage = siteMediaService.create(image);
        Assertions.assertNotNull(savedImage.getId());
        SiteMedia imageByShortCode = siteMediaService.findOne(savedImage.getPortalShortcode(),
                savedImage.getCleanFileName(), savedImage.getVersion()).get();
        Assertions.assertEquals(savedImage.getId(), imageByShortCode.getId());
    }

    @Test
    @Transactional
    public void testFindMetadataByPortal(TestInfo testInfo) {
        SiteMedia image = siteMediaFactory.builderWithDependencies(getTestName(testInfo))
                .data("imageData".getBytes()).build();
        SiteMedia savedImage = siteMediaService.create(image);
        Portal emptyPortal = portalFactory.buildPersisted(getTestName(testInfo));
        assertThat(siteMediaService.findMetadataByPortal(emptyPortal.getShortcode()),
                hasSize(0));
        List<SiteMediaMetadata> imageList = siteMediaService.findMetadataByPortal((image.getPortalShortcode()));
        assertThat(imageList, hasSize(1));
        assertThat(imageList.get(0).getCleanFileName(), equalTo(savedImage.getCleanFileName()));
    }

    @Test
    @Transactional
    public void testSanitizesCleanFileName(TestInfo info) {
        String dirtyFileName = getTestName(info) + " with spaces.png";
        SiteMedia image = siteMediaFactory.builderWithDependencies(getTestName(info))
                .cleanFileName(dirtyFileName)
                .build();
        SiteMedia savedImage = siteMediaService.create(image);
        assertThat(savedImage.getCleanFileName(), equalTo(SiteMediaService.cleanFileName(dirtyFileName)));
    }
}
