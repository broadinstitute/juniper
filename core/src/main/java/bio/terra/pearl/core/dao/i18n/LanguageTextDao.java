package bio.terra.pearl.core.dao.i18n;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.i18n.LanguageText;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class LanguageTextDao extends BaseMutableJdbiDao<LanguageText> {

    @Override
    protected Class<LanguageText> getClazz() {
        return LanguageText.class;
    }

    public LanguageTextDao(Jdbi jdbi) {
        super(jdbi);
    }

    public Optional<LanguageText> findSystemTextByKeyAndLanguage(String keyName, String language) {
        return jdbi.withHandle(handle ->
                handle.createQuery("select * from " + tableName + " where key_name = :keyName and language = :language and portal_id is null and localized_site_content_id is null")
                        .bind("keyName", keyName)
                        .bind("language", language)
                        .mapTo(clazz)
                        .findOne()
        );
    }

    public void deleteByKeyNameAndPortal(String keyName, UUID portalId) {
        deleteByTwoProperties("key_name", keyName, "portal_id", portalId);
    }

    //returns all language texts for a portal environment, with overrides for the given language
    public List<LanguageText> findWithOverridesByPortalEnvId(UUID portalEnvId, String language) {
        return jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                            // distinct on key_name and order by localized_site_content_id is used to ensure that
                            // if there are conflicting keys, the one with localized_site_content_id will be used
                            """
                                    SELECT DISTINCT ON(lt.key_name) lt.* FROM language_text lt
                                    WHERE (
                                        lt.localized_site_content_id IN (select lsc.id from portal_environment pe
                                                                          inner join site_content sc on pe.site_content_id = sc.id
                                                                          inner join localized_site_content lsc on sc.id = lsc.site_content_id
                                                                          where pe.id = :portalEnvId )
                                        OR lt.portal_id = (select portal_id from portal_environment where id = :portalEnvId)
                                        OR (lt.localized_site_content_id IS NULL and lt.portal_id IS NULL)
                                    ) AND lt.language = :language
                                    ORDER BY lt.key_name, lt.localized_site_content_id, lt.portal_id ASC
                                    """)
                        .bind("portalEnvId", portalEnvId)
                    .bind("language", language)
                    .mapToBean(LanguageText.class)
                    .list());
    }

    public void deleteByPortalId(UUID portalId) {
        deleteByProperty("portal_id", portalId);
    }

    public List<LanguageText> findByPortalId(UUID id, String lang) {
        return findAllByTwoProperties("portal_id", id, "language", lang);
    }

    public void deleteByLocalSite(UUID localSiteId) {
        deleteByProperty("localized_site_content_id", localSiteId);
    }

    public List<LanguageText> findByLocalSiteId(UUID id) {
        return findAllByProperty("localized_site_content_id", id);
    }
}
