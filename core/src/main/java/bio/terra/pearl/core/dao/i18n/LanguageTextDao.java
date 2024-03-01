package bio.terra.pearl.core.dao.i18n;

import bio.terra.pearl.core.dao.BaseMutableJdbiDao;
import bio.terra.pearl.core.model.i18n.LanguageText;
import org.jdbi.v3.core.Jdbi;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class LanguageTextDao extends BaseMutableJdbiDao<LanguageText> {

    @Override
    protected Class<LanguageText> getClazz() {
        return LanguageText.class;
    }

    public LanguageTextDao(Jdbi jdbi) {
        super(jdbi);
    }

    public List<LanguageText> findByLanguage(String language) {
        return findAllByProperty("language", language);
    }

    public Optional<LanguageText> findByKeyNameAndLanguage(String keyName, String language) {
        return findByTwoProperties("key_name", keyName, "language", language);
    }
}
