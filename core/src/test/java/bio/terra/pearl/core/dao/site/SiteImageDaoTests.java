package bio.terra.pearl.core.dao.site;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.site.SiteImageFactory;
import bio.terra.pearl.core.model.site.SiteImage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;

public class SiteImageDaoTests extends BaseSpringBootTest {
    @Autowired
    private SiteImageFactory siteImageFactory;
    @Autowired
    private SiteImageDao siteImageDao;

    @Test
    @Transactional
    public void testCrud(TestInfo info) {
        SiteImage image = siteImageFactory.builderWithDependencies(getTestName(info)).build();
        SiteImage savedImage = siteImageDao.create(image);
        DaoTestUtils.assertGeneratedProperties(savedImage);

        SiteImage imageByShortCode = siteImageDao.find(savedImage.getId()).get();
        assertThat(savedImage.getId(), equalTo(imageByShortCode.getId()));

        siteImageDao.deleteByPortalShortcode(savedImage.getPortalShortcode());

        assertThat(siteImageDao.find(savedImage.getId()).isEmpty(), is(true));
    }

    @Test
    @Transactional
    public void testFindByCleanFileName() {
        SiteImage image = siteImageFactory.builderWithDependencies("siteImageTestFindByStableId").build();
        SiteImage savedImage = siteImageDao.create(image);
        DaoTestUtils.assertGeneratedProperties(savedImage);

        SiteImage updatedImage =  new SiteImage();
        BeanUtils.copyProperties(image, updatedImage);
        updatedImage.setVersion(2);
        updatedImage.setData("updated".getBytes());
        SiteImage savedUpdatedImage = siteImageDao.create(updatedImage);

        SiteImage imageByShortCode = siteImageDao.findOne(savedImage.getPortalShortcode(),
                savedImage.getCleanFileName(), savedImage.getVersion()  ).get();
        assertThat(imageByShortCode, samePropertyValuesAs(savedImage));

        SiteImage updatedImageByShortcode = siteImageDao.findOne(savedImage.getPortalShortcode(),
                savedImage.getCleanFileName(), 2).get();
        assertThat(updatedImageByShortcode, samePropertyValuesAs(savedUpdatedImage));
    }
}
