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

    public Optional<LanguageText> findByKeyNameAndLanguage(String keyName, String language) {
        return findByTwoProperties("key_name", keyName, "language", language);
    }

    public void deleteByKeyNameAndPortal(String keyName, UUID portalId) {
        deleteByTwoProperties("key_name", keyName, "portal_id", portalId);
    }

    //returns all language texts that are either global or portal specific
    public List<LanguageText> findByPortalIdOrNullPortalId(UUID portalId, String language) {
        return jdbi.withHandle(
            handle ->
                handle
                    .createQuery(
                        "SELECT * FROM language_text WHERE (portal_id = :portalId OR portal_id IS NULL) AND language = :language")
                    .bind("portalId", portalId)
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
}
