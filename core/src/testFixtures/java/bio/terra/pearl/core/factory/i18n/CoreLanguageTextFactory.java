package bio.terra.pearl.core.factory.i18n;

import bio.terra.pearl.core.model.i18n.CoreLanguageText;
import bio.terra.pearl.core.service.i18n.CoreLanguageTextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CoreLanguageTextFactory {

    @Autowired
    private CoreLanguageTextService coreLanguageTextService;

    public CoreLanguageText.CoreLanguageTextBuilder builder(String testName, String keyName, String language) {
        return CoreLanguageText.builder()
                .language(language)
                .keyName(testName + keyName)
                .text(testName + " text");
    }

    public CoreLanguageText buildPersisted(String testName, String keyName, String language) {
        return coreLanguageTextService.create(builder(testName, keyName, language).build());
    }

}
