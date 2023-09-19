package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


public abstract class BasePopulator<T extends BaseEntity, D extends T, P extends FilePopulateContext> {
    // in general, we use constructor injection, but for widely-used and inherited beans with no complex dependencies
    // annotation injection saves a lot of lines of code
    @Autowired
    protected FilePopulateService filePopulateService;
    @Autowired
    protected ObjectMapper objectMapper;

    @Transactional
    public T populate(P context, boolean overwrite) throws IOException {
        String fileString = filePopulateService.readFile(context.getRootFileName(), context);
        return populateFromString(fileString, context, overwrite);
    }

    public T populateFromString(String fileString, P context, boolean overwrite) throws IOException {
        D popDto = readValue(fileString);
        return populateFromDto(popDto, context, overwrite);
    }

    public D readValue(String popDtoString) throws IOException {
        return objectMapper.readValue(popDtoString, getDtoClazz());
    }

    public T populateFromDto(D popDto, P context, boolean overwrite) throws IOException {
        preProcessDto(popDto, context);
        Optional<T> existingObjOpt = findFromDto(popDto, context);
        T newObj;
        if (existingObjOpt.isPresent()) {
            if (overwrite) {
                newObj = overwriteExisting(existingObjOpt.get(), popDto, context);
            } else {
                newObj = createPreserveExisting(existingObjOpt.get(), popDto, context);
            }
        } else {
            newObj = createNew(popDto, context, overwrite);
        }
        context.markFilenameAsPopulated(context.getBasePath() + "/" + context.getRootFileName(),
                newObj.getId());
        return newObj;
    }

    protected void preProcessDto(D popDto, P context) throws IOException {
        // default is no-op
    }

    protected abstract Class<D> getDtoClazz();

    public abstract Optional<T> findFromDto(D popDto, P context);

    public abstract T overwriteExisting(T existingObj, D popDto, P context) throws IOException;
    public abstract T createPreserveExisting(T existingObj, D popDto, P context) throws IOException;
    public abstract T createNew(D popDto, P context, boolean overwrite) throws IOException;
}
