package bio.terra.pearl.core.factory.i18n;

import bio.terra.pearl.core.model.i18n.CoreLanguageText;
import bio.terra.pearl.core.service.i18n.CoreLanguageTextService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CoreLanguageTextFactory {

    @Autowired
    private CoreLanguageTextService coreLanguageTextService;

    public CoreLanguageText.CoreLanguageTextBuilder builder(String testName, String language) {
        return CoreLanguageText.builder()
                .language(language)
                .i18nKey(testName + " key")
                .text(testName + " text");
    }

    public CoreLanguageText buildPersisted(String testName, String language) {
        return coreLanguageTextService.create(builder(testName, language).build());
    }

}
