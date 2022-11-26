package bio.terra.pearl.populate.service;

import bio.terra.pearl.core.model.BaseEntity;

import java.io.IOException;

public interface Populator<T extends BaseEntity> {
    public T populate(String filePathName)  throws IOException;
}
