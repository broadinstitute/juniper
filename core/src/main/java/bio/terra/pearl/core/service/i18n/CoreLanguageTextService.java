package bio.terra.pearl.core.service.i18n;

import bio.terra.pearl.core.dao.i18n.CoreLanguageTextDao;
import bio.terra.pearl.core.model.i18n.CoreLanguageText;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class CoreLanguageTextService extends CrudService<CoreLanguageText, CoreLanguageTextDao> {

    private CoreLanguageTextDao coreLanguageTextDao;

    public CoreLanguageTextService(CoreLanguageTextDao coreLanguageTextDao) {
        super(coreLanguageTextDao);
        this.coreLanguageTextDao = coreLanguageTextDao;
    }

    @Cacheable(value = "coreLanguageTexts", key = "#language")
    public HashMap<String, String> getLanguageTextMapForLanguage(String language) {
        List<CoreLanguageText> languageTexts = coreLanguageTextDao.findByLanguage(language);

        HashMap<String, String> languageTextMap = new HashMap<>();
        for (CoreLanguageText coreLanguageText : languageTexts) {
            languageTextMap.put(coreLanguageText.getKeyName(), coreLanguageText.getText());
        }

        return languageTextMap;
    }

    public Optional<CoreLanguageText> findByKeyNameAndLanguage(String keyName, String language) {
        return coreLanguageTextDao.findByKeyNameAndLanguage(keyName, language);
    }

}
