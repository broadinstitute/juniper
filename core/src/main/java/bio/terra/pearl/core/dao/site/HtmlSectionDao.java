package bio.terra.pearl.core.dao.site;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.site.HtmlSection;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class HtmlSectionDao  extends BaseJdbiDao<HtmlSection> {
    public HtmlSectionDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<HtmlSection> getClazz() {
        return HtmlSection.class;
    }

    public void deleteByPageId(UUID htmlPageId) {
        deleteByUuidProperty("html_page_id", htmlPageId);
    }
}
