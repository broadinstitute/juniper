package bio.terra.pearl.core.service.i18n;

import bio.terra.pearl.core.dao.i18n.LanguageTextDao;
import bio.terra.pearl.core.model.i18n.LanguageText;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.CrudService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class LanguageTextService extends CrudService<LanguageText, LanguageTextDao> {

    private final LanguageTextDao languageTextDao;

    public LanguageTextService(LanguageTextDao languageTextDao) {
        super(languageTextDao);
        this.languageTextDao = languageTextDao;
    }

    public HashMap<String, String> getLanguageTextMapForLanguage(UUID portalEnvId, String language) {
        List<LanguageText> languageTexts = languageTextDao.findWithOverridesByPortalEnvId(portalEnvId, language);

        HashMap<String, String> languageTextMap = new HashMap<>();
        for (LanguageText languageText : languageTexts) {
            languageTextMap.put(languageText.getKeyName(), languageText.getText());
        }

        return languageTextMap;
    }

    public void deleteByPortalId(UUID portalId) {
        languageTextDao.deleteByPortalId(portalId);
    }

    public Optional<LanguageText> findSystemTextByKeyAndLanguage(String keyName, String language) {
        return languageTextDao.findSystemTextByKeyAndLanguage(keyName, language);
    }

    public void deleteByLocalSite(UUID localSiteId, Set<CascadeProperty> cascades) {
        languageTextDao.deleteByLocalSite(localSiteId);
    }

    public Optional<LanguageText> findBySiteContentLanguageAndKey(UUID siteContentId, String language, String key) {
        return languageTextDao.findBySiteContentLanguageAndKey(siteContentId, language, key);
    }
}
