package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.dao.kit.KitRequestDao;
import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.model.kit.KitRequest;
import bio.terra.pearl.populate.dto.kit.KitRequestDto;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

// TODO: finish implementing this
@Service
public class KitRequestPopulator extends BasePopulator<KitRequest, KitRequestDto, FilePopulateContext> {
    private final KitRequestDao kitRequestDao;
    private final KitTypeDao kitTypeDao;

    public KitRequestPopulator(KitRequestDao kitRequestDao,
                               KitTypeDao kitTypeDao) {
        this.kitRequestDao = kitRequestDao;
        this.kitTypeDao = kitTypeDao;
    }

    @Override
    protected Class<KitRequestDto> getDtoClazz() {
        return KitRequestDto.class;
    }

    @Override
    public KitRequest createNew(KitRequestDto popDto, FilePopulateContext context, boolean overwrite) throws IOException {
        var kitType = kitTypeDao.findByName(popDto.getKitTypeName()).get();
        var kitRequest = KitRequest.builder()
                .kitTypeId(kitType.getId())
                .build();
        return kitRequestDao.create(kitRequest);
    }

    @Override
    public KitRequest overwriteExisting(KitRequest existingObj, KitRequestDto popDto, FilePopulateContext context) throws IOException {
        return null;
    }

    @Override
    public Optional<KitRequest> findFromDto(KitRequestDto popDto, FilePopulateContext context) {
        return Optional.empty();
    }

    @Override
    public KitRequest createPreserveExisting(KitRequest existingObj, KitRequestDto popDto, FilePopulateContext context) throws IOException {
        return null;
    }
}
