package bio.terra.pearl.core.dao.site;

import bio.terra.pearl.core.BaseSpringBootTest;
import bio.terra.pearl.core.factory.DaoTestUtils;
import bio.terra.pearl.core.factory.site.SiteMediaFactory;
import bio.terra.pearl.core.model.site.SiteMedia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;

public class SiteMediaDaoTests extends BaseSpringBootTest {
    @Autowired
    private SiteMediaFactory siteMediaFactory;
    @Autowired
    private SiteMediaDao siteMediaDao;

    @Test
    @Transactional
    public void testCrud(TestInfo info) {
        SiteMedia image = siteMediaFactory.builderWithDependencies(getTestName(info)).build();
        SiteMedia savedImage = siteMediaDao.create(image);
        DaoTestUtils.assertGeneratedProperties(savedImage);

        SiteMedia imageByShortCode = siteMediaDao.find(savedImage.getId()).get();
        assertThat(savedImage.getId(), equalTo(imageByShortCode.getId()));

        siteMediaDao.deleteByPortalShortcode(savedImage.getPortalShortcode());

        assertThat(siteMediaDao.find(savedImage.getId()).isEmpty(), is(true));
    }

    @Test
    @Transactional
    public void testFindByCleanFileName(TestInfo info) {
        SiteMedia image = siteMediaFactory.builderWithDependencies(getTestName(info)).build();
        SiteMedia savedImage = siteMediaDao.create(image);
        DaoTestUtils.assertGeneratedProperties(savedImage);

        SiteMedia updatedImage = new SiteMedia();
        BeanUtils.copyProperties(image, updatedImage);
        updatedImage.setVersion(2);
        updatedImage.setData("updated".getBytes());
        SiteMedia savedUpdatedImage = siteMediaDao.create(updatedImage);

        SiteMedia imageByShortCode = siteMediaDao.findOne(savedImage.getPortalShortcode(),
                savedImage.getCleanFileName(), savedImage.getVersion()  ).get();
        assertThat(imageByShortCode, samePropertyValuesAs(savedImage));

        SiteMedia updatedImageByShortcode = siteMediaDao.findOne(savedImage.getPortalShortcode(),
                savedImage.getCleanFileName(), 2).get();
        assertThat(updatedImageByShortcode, samePropertyValuesAs(savedUpdatedImage));
    }
}
