package bio.terra.pearl.core.dao.site;

import bio.terra.pearl.core.dao.BaseJdbiDao;
import bio.terra.pearl.core.model.site.SiteMedia;
import bio.terra.pearl.core.model.site.SiteMediaMetadata;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.mapper.reflect.BeanMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class SiteMediaDao extends BaseJdbiDao<SiteMedia> {
    private String metadataFieldString;
    private RowMapper imageMetadataRowMapper = BeanMapper.of(SiteMediaMetadata.class);

    public SiteMediaDao(Jdbi jdbi) {
        super(jdbi);
        jdbi.registerRowMapper(SiteMediaMetadata.class, imageMetadataRowMapper);
        List<String> metadataColumns = getGetQueryColumns();
        metadataColumns.remove("data");
        metadataFieldString = metadataColumns.stream().collect(Collectors.joining(", "));
    }

    @Override
    protected Class<SiteMedia> getClazz() {
        return SiteMedia.class;
    }

    public Optional<SiteMedia> findOne(String portalShortcode, String cleanFileName, int version) {
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

    public Optional<SiteMedia> findOneLatestVersion(String portalShortcode, String cleanFileName) {
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

    public List<SiteMedia> findByPortal(String portalShortcode) {
        return findAllByProperty("portal_shortcode", portalShortcode);
    }

    public List<SiteMediaMetadata> findMetadataByPortal(String portalShortcode) {
        return jdbi.withHandle(handle ->
                handle.createQuery("""
                                select %s from %s 
                                where portal_shortcode = :portalShortcode
                                """.formatted(metadataFieldString, tableName))
                        .bind("portalShortcode", portalShortcode)
                        .mapTo(SiteMediaMetadata.class)
                        .list()
        );
    }

    public void deleteByPortalShortcode(String portalShortcode) {
        deleteByProperty("portal_shortcode", portalShortcode);
    }
}
