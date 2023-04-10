package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.Environment;
import bio.terra.pearl.core.service.EnvironmentService;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class EnvironmentPopulator extends Populator<Environment, Environment, FilePopulateContext> {
    private EnvironmentService environmentService;

    public EnvironmentPopulator(EnvironmentService environmentService) {
        this.environmentService = environmentService;
    }

    @Override
    protected Class<Environment> getDtoClazz() {
        return Environment.class;
    }

    @Override
    public Environment createNew(Environment popDto, FilePopulateContext context, boolean overwrite) {
        return environmentService.create(popDto);
    }

    @Override
    public Environment createPreserveExisting(Environment existingObj, Environment popDto, FilePopulateContext context) {
        // there's nothing in environments yet except the name, so just leave as-is
        return existingObj;
    }

    @Override
    public Environment overwriteExisting(Environment existingObj, Environment popDto, FilePopulateContext context) {
        // there's nothing in environments yet except the name, so just leave as-is
        return existingObj;
    }

    @Override
    public Optional<Environment> findFromDto(Environment popDto, FilePopulateContext context) {
        return environmentService.findOneByName(popDto.getName());
    }
}
