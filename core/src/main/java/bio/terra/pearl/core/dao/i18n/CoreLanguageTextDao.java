package bio.terra.pearl.core.dao.i18n;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.i18n.CoreLanguageText;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CoreLanguageTextDao extends BaseMutableJdbiDao<CoreLanguageText> {

    @Override
    protected Class<CoreLanguageText> getClazz() {
        return CoreLanguageText.class;
    }

    public CoreLanguageTextDao(Jdbi jdbi) {
        super(jdbi);
    }

    public List<CoreLanguageText> findByLanguage(String language) {
        return findAllByProperty("language", language);
    }

    public Optional<CoreLanguageText> findByKeyNameAndLanguage(String keyName, String language) {
        return findByTwoProperties("key_name", keyName, "language", language);
    }
}
