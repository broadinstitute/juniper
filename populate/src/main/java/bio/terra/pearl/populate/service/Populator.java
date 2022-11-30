package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.BaseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;


public abstract class Populator<T extends BaseEntity> {
    protected FilePopulateService filePopulateService;
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
