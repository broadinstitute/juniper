package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.BaseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;


public abstract class Populator<T extends BaseEntity> {
    // in general, we use constructor injection, but for widely-used and inherited beans with no complex dependencies
    // annotation injection saves a lot of lines of code
    @Autowired
    protected FilePopulateService filePopulateService;
    @Autowired
    protected ObjectMapper objectMapper;

    public T populate(String filePathName) throws IOException {
        FilePopulateConfig config = new FilePopulateConfig(filePathName);
        return populate(config);
    }

    public T populate(FilePopulateConfig config) throws IOException {
        String fileString = filePopulateService.readFile(config.getRootFileName(), config);
        return populateFromString(fileString, config);
    }

    public abstract T populateFromString(String fileString, FilePopulateConfig config) throws IOException;
}
