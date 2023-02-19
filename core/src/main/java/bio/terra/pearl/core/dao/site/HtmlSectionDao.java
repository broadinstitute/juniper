package bio.terra.pearl.core.dao.site;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.site.HtmlSection;
import org.apache.commons.lang3.StringUtils;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class HtmlSectionDao  extends BaseJdbiDao<HtmlSection> {
    public HtmlSectionDao(Jdbi jdbi) {
        super(jdbi);
    }

    @Override
    protected Class<HtmlSection> getClazz() {
        return HtmlSection.class;
    }

    /** gets all the sections for a site, sorted */
    public List<HtmlSection> findByLocalizedSite(UUID localSiteId) {
        List<String> primaryCols = getQueryColumns.stream().map(col -> "a." + col)
                .collect(Collectors.toList());
        return jdbi.withHandle(handle ->
                handle.createQuery("select " + StringUtils.join(primaryCols, ", ") + " from " + tableName
                                + " a join html_page b on html_page_id = b.id"
                                + " where b.localized_site_content_id = :localSiteId order by a.section_order;")
                        .bind("localSiteId", localSiteId)
                        .mapTo(clazz)
                        .list()
        );
    }

    public void deleteByPageId(UUID htmlPageId) {
        deleteByProperty("html_page_id", htmlPageId);
    }
}
