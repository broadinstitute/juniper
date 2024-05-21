package bio.terra.pearl.core.service.i18n;

import bio.terra.pearl.core.dao.i18n.LanguageTextDao;
import bio.terra.pearl.core.model.i18n.LanguageText;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LanguageTextService extends CrudService<LanguageText, LanguageTextDao> {

    private LanguageTextDao languageTextDao;

    public LanguageTextService(LanguageTextDao languageTextDao) {
        super(languageTextDao);
        this.languageTextDao = languageTextDao;
    }

    @Cacheable(value = "languageTexts", key = "#language")
    public HashMap<String, String> getLanguageTextMapForLanguage(UUID portalId, String language) {
        List<LanguageText> languageTexts = languageTextDao.findByPortalIdOrNullPortalId(portalId, language);

        HashMap<String, String> languageTextMap = new HashMap<>();
        for (LanguageText languageText : languageTexts) {
            languageTextMap.put(languageText.getKeyName(), languageText.getText());
        }

        return languageTextMap;
    }

    public void deleteByPortalId(UUID portalId) {
        languageTextDao.deleteByPortalId(portalId);
    }

    public Optional<LanguageText> findByKeyNameAndLanguage(String keyName, String language) {
        return languageTextDao.findByKeyNameAndLanguage(keyName, language);
    }

}
