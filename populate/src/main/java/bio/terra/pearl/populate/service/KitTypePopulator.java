package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.dao.kit.KitTypeDao;
import bio.terra.pearl.core.model.kit.KitType;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
public class KitTypePopulator extends BasePopulator<KitType, KitType, FilePopulateContext> {
    private final KitTypeDao kitTypeDao;

    public KitTypePopulator(KitTypeDao kitTypeDao) {
        this.kitTypeDao = kitTypeDao;
    }

    @Override
    protected Class<KitType> getDtoClazz() {
        return KitType.class;
    }

    @Override
    public KitType createNew(KitType popDto, FilePopulateContext context, boolean overwrite) throws IOException {
        return kitTypeDao.create(popDto);
    }

    @Override
    public Optional<KitType> findFromDto(KitType popDto, FilePopulateContext context) {
        return kitTypeDao.findByName(popDto.getName());
    }

    @Override
    public KitType createPreserveExisting(KitType existingObj, KitType popDto, FilePopulateContext context) throws IOException {
        existingObj.setDisplayName(popDto.getDisplayName());
        existingObj.setDescription(popDto.getDescription());
        kitTypeDao.update(existingObj);
        return existingObj;
    }

    @Override
    public KitType overwriteExisting(KitType existingObj, KitType popDto, FilePopulateContext context) throws IOException {
        kitTypeDao.delete(existingObj.getId());
        return kitTypeDao.create(popDto);
    }
}
