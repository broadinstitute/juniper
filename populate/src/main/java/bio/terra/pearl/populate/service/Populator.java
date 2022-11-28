package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.BaseEntity;

import java.io.IOException;

public interface Populator<T extends BaseEntity> {
    public default T populate(String filePathName) throws IOException {
        FilePopulateConfig config = new FilePopulateConfig(filePathName);
        return populate(config.getRootFileName(), config);
    }

    public default T populate(String fileName, FilePopulateConfig config) throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }
}
