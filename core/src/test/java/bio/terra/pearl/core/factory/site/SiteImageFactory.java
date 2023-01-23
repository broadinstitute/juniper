package bio.terra.pearl.core.factory.site;

import bio.terra.pearl.core.model.site.SiteContent;
import bio.terra.pearl.core.model.site.SiteImage;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;

@Component
public class SiteImageFactory {
    @Autowired
    private SiteContentFactory siteContentFactory;

    public SiteImage.SiteImageBuilder builder(String testName) {
        return SiteImage.builder().data("abc123".getBytes(Charset.defaultCharset()))
                .uploadFileName(testName + RandomStringUtils.randomAlphabetic(3) + ".png");
    }

    public SiteImage.SiteImageBuilder builderWithDependencies(String testName) {
        SiteContent baseContent = siteContentFactory.buildPersisted(testName);
        return builder(testName)
                .siteContentId(baseContent.getId());
    }
}
