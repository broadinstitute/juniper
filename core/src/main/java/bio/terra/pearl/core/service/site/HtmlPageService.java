package bio.terra.pearl.core.service.site;

import bio.terra.pearl.core.dao.site.HtmlPageDao;
import bio.terra.pearl.core.dao.site.HtmlSectionDao;
import bio.terra.pearl.core.model.site.HtmlPage;
import bio.terra.pearl.core.model.site.HtmlSection;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class HtmlPageService extends CrudService<HtmlPage, HtmlPageDao> {
    private HtmlSectionDao htmlSectionDao;

    public HtmlPageService(HtmlPageDao dao, HtmlSectionDao htmlSectionDao) {
        super(dao);
        this.htmlSectionDao = htmlSectionDao;
    }

    @Override
    public HtmlPage create(HtmlPage page) {
        HtmlPage savedPage = dao.create(page);
        for (int i = 0; i < page.getSections().size(); i++) {
            HtmlSection section = page.getSections().get(i);
            section.setSectionOrder(i);
            section.setHtmlPageId(savedPage.getId());
            savedPage.getSections().add(htmlSectionDao.create(section));
        }
        return savedPage;
    }

    @Override
    public void delete(UUID pageId, Set<CascadeProperty> cascades) {
        htmlSectionDao.deleteByPageId(pageId);
        dao.delete(pageId);
    }

    public void deleteByLocalSite(UUID localSiteId, Set<CascadeProperty> cascades) {
        List<HtmlPage> pages = dao.findByLocalSite(localSiteId);
        for (HtmlPage page : pages) {
            delete(page.getId(), cascades);
        }
    }
}
