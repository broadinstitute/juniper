package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.i18n.CoreLanguageText;
import bio.terra.pearl.core.service.CascadeProperty;
import bio.terra.pearl.core.service.i18n.CoreLanguageTextService;
import bio.terra.pearl.populate.dto.CoreLanguageTextDto;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class CoreLanguageTextPopulator extends BasePopulator<CoreLanguageText, CoreLanguageTextDto, FilePopulateContext> {

    private CoreLanguageTextService coreLanguageTextService;

    public CoreLanguageTextPopulator(CoreLanguageTextService coreLanguageTextService) {
        this.coreLanguageTextService = coreLanguageTextService;
    }

    @Override
    protected Class<CoreLanguageTextDto> getDtoClazz() { return CoreLanguageTextDto.class; }

    @Override
    public Optional<CoreLanguageText> findFromDto(CoreLanguageTextDto popDto, FilePopulateContext context) {
        return coreLanguageTextService.findByKeyNameAndLanguage(popDto.getKeyName(), popDto.getLanguage());
    }

    @Override
    public CoreLanguageText overwriteExisting(CoreLanguageText existingObj, CoreLanguageTextDto popDto, FilePopulateContext context) throws IOException {
        coreLanguageTextService.delete(existingObj.getId(), CascadeProperty.EMPTY_SET);
        return createNew(popDto, context, true);
    }

    @Override
    public CoreLanguageText createPreserveExisting(CoreLanguageText existingObj, CoreLanguageTextDto popDto, FilePopulateContext context) throws IOException {
        popDto.setId(existingObj.getId());
        return coreLanguageTextService.update(popDto);
    }

    @Override
    public CoreLanguageText createNew(CoreLanguageTextDto popDto, FilePopulateContext context, boolean overwrite) throws IOException {
        return coreLanguageTextService.create(popDto);
    }
}
