package bio.terra.pearl.core.factory.i18n;

import bio.terra.pearl.core.model.i18n.LanguageText;
import bio.terra.pearl.core.service.i18n.LanguageTextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LanguageTextFactory {

    @Autowired
    private LanguageTextService languageTextService;

    public LanguageText.LanguageTextBuilder builder(String testName, String keyName, String language) {
        return LanguageText.builder()
                .language(language)
                .keyName(testName + keyName)
                .text(testName + " text");
    }

    public LanguageText buildPersisted(String testName, String keyName, String language) {
        return languageTextService.create(builder(testName, keyName, language).build());
    }

}
