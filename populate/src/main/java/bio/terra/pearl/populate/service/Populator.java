package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.BaseEntity;
import bio.terra.pearl.populate.service.contexts.FilePopulateContext;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;


public abstract class Populator<T extends BaseEntity, P extends FilePopulateContext> {
    // in general, we use constructor injection, but for widely-used and inherited beans with no complex dependencies
    // annotation injection saves a lot of lines of code
    @Autowired
    protected FilePopulateService filePopulateService;
    @Autowired
    protected ObjectMapper objectMapper;

    /** nukes and recreates the given object */
    @Transactional
    public T populate(P context) throws IOException {
        String fileString = filePopulateService.readFile(context.getRootFileName(), context);
        return populateFromString(fileString, context);
    }

    /** updates the object in-place where possible */
    @Transactional
    public T update(P context) throws IOException {
        String fileString = filePopulateService.readFile(context.getRootFileName(), context);
        return updateFromString(fileString, context);
    }

    public abstract T populateFromString(String fileString, P context) throws IOException;

    public abstract T updateFromString(String fileString, P context) throws IOException;
}
