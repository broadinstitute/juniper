package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.i18n.LanguageText;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.i18n.LanguageTextService;
import bio.terra.pearl.populate.dto.LanguageTextDto;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class LanguageTextPopulator extends BasePopulator<LanguageText, LanguageTextDto, FilePopulateContext> {

    private LanguageTextService languageTextService;

    public LanguageTextPopulator(LanguageTextService languageTextService) {
        this.languageTextService = languageTextService;
    }

    @Override
    protected Class<LanguageTextDto> getDtoClazz() { return LanguageTextDto.class; }

    @Override
    public Optional<LanguageText> findFromDto(LanguageTextDto popDto, FilePopulateContext context) {
        return languageTextService.findByKeyNameAndLanguage(popDto.getKeyName(), popDto.getLanguage());
    }

    @Override
    public LanguageText overwriteExisting(LanguageText existingObj, LanguageTextDto popDto, FilePopulateContext context) throws IOException {
        languageTextService.delete(existingObj.getId(), CascadeProperty.EMPTY_SET);
        return createNew(popDto, context, true);
    }

    @Override
    public LanguageText createPreserveExisting(LanguageText existingObj, LanguageTextDto popDto, FilePopulateContext context) throws IOException {
        popDto.setId(existingObj.getId());
        return languageTextService.update(popDto);
    }

    @Override
    public LanguageText createNew(LanguageTextDto popDto, FilePopulateContext context, boolean overwrite) throws IOException {
        return languageTextService.create(popDto);
    }
}
