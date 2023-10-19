package bio.terra.pearl.core.dao.site;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.site.SiteImage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import bio.terra.pearl.core.model.site.SiteImageMetadata;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Component;

@Component
public class SiteImageDao extends BaseJdbiDao<SiteImage> {
    private String fieldStringWithoutDataColumn;
    private RowMapper imageMetadataRowMapper = BeanMapper.of(SiteImageMetadata.class);
    public SiteImageDao(Jdbi jdbi) {
        super(jdbi);
        List<String> colsWithoutDataCol = getGetQueryColumns();
        jdbi.registerRowMapper(SiteImageMetadata.class, imageMetadataRowMapper);
        colsWithoutDataCol.remove("data");
        fieldStringWithoutDataColumn = colsWithoutDataCol.stream().collect(Collectors.joining(", "));
    }

    @Override
    protected Class<SiteImage> getClazz() {
        return SiteImage.class;
    }

    public Optional<SiteImage> findOne(String portalShortcode, String cleanFileName, int version) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " where portal_shortcode = :portalShortcode"
                                + " and clean_file_name = :cleanFileName and version = :version;")
                        .bind("portalShortcode", portalShortcode)
                        .bind("cleanFileName", cleanFileName)
                        .bind("version", version)
                        .mapTo(clazz)
                        .findOne()
        );
    }

    public Optional<SiteImage> findOneLatestVersion(String portalShortcode, String cleanFileName) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " where portal_shortcode = :portalShortcode"
                                + " and clean_file_name = :cleanFileName order by version desc limit 1;")
                        .bind("portalShortcode", portalShortcode)
                        .bind("cleanFileName", cleanFileName)
                        .mapTo(clazz)
                        .findOne()
        );
    }

    public int getNextVersion(String cleanFileName, String portalShortcode) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select max(version) from " + tableName + " where clean_file_name = :cleanFileName" +
                                " and portal_shortcode = :portalShortcode")
                        .bind("cleanFileName", cleanFileName)
                        .bind("portalShortcode", portalShortcode)
                        .mapTo(int.class)
                        .one()
        ) + 1;
    }

    public List<SiteImageMetadata> findMetadataByPortal(String portalShortcode) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                select %s from %s 
                                where portal_shortcode = :portalShortcode
                                """.formatted(fieldStringWithoutDataColumn, tableName))
                        .bind("portalShortcode", portalShortcode)
                        .mapTo(SiteImageMetadata.class)
                        .list()
        );
    }

    public void deleteByPortalShortcode(String portalShortcode) {
        deleteByProperty("portal_shortcode", portalShortcode);
    }
}
